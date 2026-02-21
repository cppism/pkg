package pkg.distributions.fedora

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pkg.ReleaseName
import pkg.Version
import pkg.distributions.ApiClient
import pkg.distributions.fedora.api.PackageInfo
import pkg.settings.DistributionPackage
import pkg.settings.Settings

class FedoraApiClient(
    distributionSettings: Settings.Fedora,
    communicationsSettings: Settings.Communications,
) : ApiClient<DistributionPackage>(
    communicationsSettings = communicationsSettings,
    defaultUrl = distributionSettings.url,
) {
    private val releaseNamePattern = Regex(
        distributionSettings.releaseNamePattern
    )

    private val mutex = Mutex()

    private var releases: List<ReleaseName>? = null

    private suspend fun getReleases(): List<ReleaseName> {
        return httpClient.get(
            urlString = "branches"
        )
            .body<List<String>>()
            .filter(releaseNamePattern::matches)
            .map(::ReleaseName)
            .reversed()
    }

    override suspend fun getPackageVersions(
        distributionPackage: DistributionPackage,
    ): Map<ReleaseName, Version> =
        mutex.withLock {
            if (releases == null)
                releases = getReleases()
            checkNotNull(releases)
        }
            .associateWith { releaseName: ReleaseName ->
                val packageInfo: PackageInfo =
                    httpClient.get(
                        urlString = "$releaseName/pkg/${distributionPackage.name}/",
                    )
                        .body()
                Version.fromString(packageInfo.version)
            }
}