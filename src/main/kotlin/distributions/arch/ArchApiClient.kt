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
    private val distributionSettings: Settings.Arch,
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

    override suspend fun getPackageVersions(
        distributionPackage: ArchDistributionPackage,
    ): Map<ReleaseName, Version> {
        val packageData: PackageData =
            httpClient.get(
                urlString = getPackageUrl(distributionPackage),
            )
                .body<PackageData>()
        return mapOf(
            Pair(
                ReleaseName(distributionSettings.architecture),
                Version.fromString(
                    extractPackageVersion(packageData.pkgver),
                )
            )
        )
    }

    private fun getPackageUrl(
        distributionPackage: ArchDistributionPackage,
    ): String =
        "packages/" +
                "${distributionPackage.source}/" +
                "${distributionSettings.architecture}/" +
                "${distributionPackage.name}/" +
                "json/"
}