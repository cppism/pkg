package pkg.distributions.arch

import io.ktor.client.call.*
import io.ktor.client.request.*
import pkg.ReleaseName
import pkg.Version
import pkg.distributions.ApiClient
import pkg.distributions.arch.api.PackageData
import pkg.settings.ArchDistributionPackage
import pkg.settings.Settings

class ArchApiClient(
    distributionSettings: Settings.Arch,
    communicationsSettings: Settings.Communications,
) : ApiClient<ArchDistributionPackage>(
    communicationsSettings = communicationsSettings,
    defaultUrl = distributionSettings.url,
) {
    private companion object {
        fun extractPackageVersion(
            value: String,
        ): String =
            value.substringBefore('+')
    }

    private val releases: List<ReleaseName> =
        listOf(
            ReleaseName(distributionSettings.architecture)
        )

    override suspend fun getReleases(): List<ReleaseName> =
        releases

    override suspend fun getPackageVersions(
        distributionPackage: ArchDistributionPackage,
    ): Map<ReleaseName, Version> =
        getReleases().associateWith { releaseName: ReleaseName ->
            Version.fromString(
                extractPackageVersion(
                    httpClient.get(
                        urlString = getPackageUrl(releaseName, distributionPackage),
                    )
                        .body<PackageData>()
                        .pkgver
                ),
            )
        }

    private fun getPackageUrl(
        releaseName: ReleaseName,
        distributionPackage: ArchDistributionPackage,
    ): String =
        "packages/" +
                "${distributionPackage.source}/" +
                "$releaseName/" +
                "${distributionPackage.name}/" +
                "json/"
}