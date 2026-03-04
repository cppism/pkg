package pkg

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import pkg.notifications.telegram.TelegramClient
import pkg.notifications.telegram.api.TelegramMessage
import pkg.serialization.PackageDeserializationStrategy
import pkg.settings.DistributionPackage
import pkg.settings.Package
import pkg.settings.Settings
import java.nio.file.Paths
import kotlin.io.path.nameWithoutExtension
import kotlin.time.Clock

val yaml = Yaml(
    configuration = YamlConfiguration(
        singleLineStringStyle = SingleLineStringStyle.Plain,
    )
)

suspend fun main() {
    val settings: Settings =
        checkNotNull(
            Settings::class.java.getResourceAsStream("/settings.yaml"),
        )
            .use(yaml::decodeFromStream)

    val storagePath = Path(settings.storage.path)
    val packagesDirectoryPath = Path(storagePath, settings.storage.packagesDirectory)
    val packagesFilePath = Path(storagePath, settings.storage.packagesFile)
    val releasesFilePath = Path(storagePath, settings.storage.releasesFile)
    val messagesDirectoryPath = Path(storagePath, settings.storage.messagesDirectory)

    val packages: Map<PackageName, Package> =
        getPackages(packagesDirectoryPath)

    val updates: Updates =
        DistributionClientsMap(settings).use { distributionClients: DistributionClientsMap ->
            val distributionsReleases: Map<Distribution, List<ReleaseName>> =
                distributionClients.mapValues { distributionClient: Map.Entry<Distribution, DistributionClient<out DistributionPackage>> ->
                    distributionClient.value.getReleases()
                }
            val packagesVersions = PackagesVersions(
                packages.mapValues { pkg: Map.Entry<PackageName, Package> ->
                    distributionClients.mapValues { distributionClient: Map.Entry<Distribution, DistributionClient<out DistributionPackage>> ->
                        distributionClient.value.getPackageVersions(pkg.value)
                    }
                }
            )
            Updates(
                releases = saveReleases(distributionsReleases, packagesVersions, releasesFilePath),
                packages = savePackages(packagesVersions, packagesFilePath),
            )
        }
    dumpMessages(
        updates,
        packages,
        messagesDirectoryPath,
        settings.notifications,
    )
    sendNotifications(messagesDirectoryPath, settings)
}

fun getPackages(
    packagesDirectoryPath: Path,
): Map<PackageName, Package> =
    SystemFileSystem.list(packagesDirectoryPath)
        .sortedBy(Path::name)
        .associateBy(
            keySelector = { packageFilePath: Path ->
                PackageName(
                    Paths.get(
                        packageFilePath.name
                    ).nameWithoutExtension
                )
            }
        )
        .mapValues { pkg: Map.Entry<PackageName, Path> ->
            FileSystem.readYamlFile(
                yaml = yaml,
                path = pkg.value,
                deserializationStrategy = PackageDeserializationStrategy(
                    packageName = pkg.key,
                ),
            )
        }

private fun savePackages(
    packagesVersions: PackagesVersions,
    packagesFilePath: Path,
): Updates.Packages {
    val from: PackagesVersions =
        try {
            FileSystem.readYamlFile(yaml, packagesFilePath)
        } catch (_: FileNotFoundException) {
            PackagesVersions(emptyMap())
        }
    FileSystem.writeYamlFile(
        value = packagesVersions,
        yaml = yaml,
        path = packagesFilePath,
    )
    return Updates.Packages(
        from = from,
        to = packagesVersions,
    )
}

private fun saveReleases(
    distributionsReleases: Map<Distribution, List<ReleaseName>>,
    packagesVersions: PackagesVersions,
    releasesFilePath: Path,
): Updates.Releases {
    val from: Map<Distribution, List<ReleaseName>> =
        try {
            FileSystem.readYamlFile(yaml, releasesFilePath)
        } catch (_: FileNotFoundException) {
            emptyMap()
        }
    FileSystem.writeYamlFile(
        value = distributionsReleases,
        yaml = yaml,
        path = releasesFilePath,
    )
    return Updates.Releases(from, distributionsReleases, packagesVersions)
}

private fun dumpMessages(
    updates: Updates,
    packages: Map<PackageName, Package>,
    messagesDirectoryPath: Path,
    settings: Settings.Notifications,
) {
    val telegramMessagesPath: Path =
        FileSystem.createDirectory(
            messagesDirectoryPath,
            "TELEGRAM",
        )
    val timestamp: Long =
        Clock.System.now().epochSeconds
    dumpUpdateMessages(
        updates.releases,
        packages,
        telegramMessagesPath,
        settings,
        timestamp + 1,
    )
    dumpUpdateMessages(
        updates.packages,
        packages,
        telegramMessagesPath,
        settings,
        timestamp + 2,
    )
}

private fun dumpUpdateMessages(
    updates: Updates.Releases,
    packages: Map<PackageName, Package>,
    telegramMessagesPath: Path,
    settings: Settings.Notifications,
    timestamp: Long,
) {
    updates.forEach { (distribution, releases) ->
        releases.forEach { (releaseName: ReleaseName, releasePackages: Map<PackageName, Version>) ->
            settings.telegram.channels.forEach { channel: String ->
                val message: TelegramMessage =
                    TelegramMessage.fromUpdate(
                        channel = channel,
                        distribution = distribution,
                        releaseName = releaseName,
                        packages = releasePackages.mapKeys {
                            packages.getValue(it.key)
                        },
                    )
                FileSystem.writeYamlFile(
                    value = message,
                    yaml = yaml,
                    path = Path(
                        telegramMessagesPath,
                        "$timestamp-${distribution.name}-$releaseName-$channel.yaml",
                    ),
                )
            }
        }
    }
}

private fun dumpUpdateMessages(
    updates: Updates.Packages,
    packages: Map<PackageName, Package>,
    telegramMessagesPath: Path,
    settings: Settings.Notifications,
    timestamp: Long,
) {
    updates.forEach { (packageName, update) ->
        val pkg: Package =
            packages.getValue(packageName)
        settings.telegram.channels.forEach { channel: String ->
            val message: TelegramMessage =
                TelegramMessage.fromUpdate(
                    channel = channel,
                    packageName = packageName,
                    packageTitle = pkg.title,
                    updates = update,
                )
            FileSystem.writeYamlFile(
                value = message,
                yaml = yaml,
                path = Path(
                    telegramMessagesPath,
                    "$timestamp-$packageName-$channel.yaml",
                ),
            )
        }
    }
}

private suspend fun sendNotifications(
    messagesDirectoryPath: Path,
    settings: Settings,
) {
    val telegramBotToken: String =
        Secrets.telegramBotToken ?: return
    TelegramClient(
        token = telegramBotToken,
        notificationsSettings = settings.notifications.telegram,
        communicationsSettings = settings.communications,
    ).use { telegramClient: TelegramClient ->
        SystemFileSystem.list(
            Path(
                messagesDirectoryPath,
                "TELEGRAM",
            )
        )
            .forEach { messagePath: Path ->
                telegramClient.sendMessage(
                    FileSystem.readYamlFile(yaml, messagePath)
                )
                SystemFileSystem.delete(messagePath)
            }
    }
}