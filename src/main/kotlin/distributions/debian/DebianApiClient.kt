package pkg.distributions.debian

import io.ktor.client.call.*
import io.ktor.client.request.*
import pkg.ReleaseName
import pkg.Version
import pkg.distributions.ApiClient
import pkg.distributions.debian.api.PackageVersion
import pkg.distributions.debian.api.PackageVersionsList
import pkg.settings.DistributionPackage
import pkg.settings.Settings

class DebianApiClient(
    distributionSettings: Settings.Debian,
    communicationsSettings: Settings.Communications,
) : ApiClient<DistributionPackage>(
    communicationsSettings = communicationsSettings,
    defaultUrl = distributionSettings.url,
) {
    private companion object {
        fun extractPackageVersion(
            value: String,
        ): String =
            value.substringBefore('-')
    }

    private val releases: List<ReleaseName> =
        ReleasesIndex.releases
            .filter(Suite::isSupported)
            .map(Suite::releaseName)

    override suspend fun getReleases(): List<ReleaseName> =
        releases

    override suspend fun getPackageVersions(
        distributionPackage: DistributionPackage,
    ): Map<ReleaseName, Version> {
        return httpClient.get(
            urlString = "src/${distributionPackage.name}/",
        )
            .body<PackageVersionsList>()
            .versions
            .flatMap { packageVersion: PackageVersion ->
                packageVersion.suites
                    .filter { suite: String ->
                        releases.any {
                            suite == it.value
                        }
                    }
                    .map { suite: String ->
                        Pair(ReleaseName(suite), packageVersion.version)
                    }
            }
            .groupBy({ it.first }) { it.second }
            .mapValues { (_, value: List<String>) ->
                Version.fromString(
                    extractPackageVersion(value.first()),
                )
            }
    }
}