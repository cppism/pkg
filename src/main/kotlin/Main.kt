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
    val messagesDirectoryPath = Path(storagePath, settings.storage.messagesDirectory)

    val packages: Map<PackageName, Package> =
        getPackages(packagesDirectoryPath)

    val updates: PackagesUpdates =
        DistributionClientsMap(settings).use { distributionClients: DistributionClientsMap ->
            val packagesVersions = PackagesVersions(
                packages.mapValues { pkg: Map.Entry<PackageName, Package> ->
                    distributionClients.mapValues { distributionClient: Map.Entry<Distribution, DistributionClient<out DistributionPackage>> ->
                        distributionClient.value.getPackageVersions(pkg.value)
                    }
                }
            )
            savePackages(packagesVersions, packagesFilePath)
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
): PackagesUpdates {
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
    return PackagesUpdates(
        from = from,
        to = packagesVersions,
    )
}

private fun dumpMessages(
    updates: PackagesUpdates,
    packages: Map<PackageName, Package>,
    messagesDirectoryPath: Path,
    settings: Settings.Notifications,
) {
    val epochSeconds: Long =
        Clock.System.now().epochSeconds
    val telegramMessagesPath: Path =
        FileSystem.createDirectory(
            messagesDirectoryPath,
            "TELEGRAM",
        )
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
            val messagePath = Path(
                telegramMessagesPath,
                "$epochSeconds-$packageName-$channel.yaml",
            )
            FileSystem.writeYamlFile(
                value = message,
                yaml = yaml,
                path = messagePath,
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