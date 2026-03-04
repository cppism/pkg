package pkg.distributions.launchpad

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNamingStrategy
import pkg.ReleaseName
import pkg.Version
import pkg.distributions.ApiClient
import pkg.distributions.launchpad.api.BinaryPackagePublishingHistory
import pkg.distributions.launchpad.api.Distribution
import pkg.distributions.launchpad.api.DistroArchSeries
import pkg.distributions.launchpad.api.DistroSeries
import pkg.settings.DistributionPackage
import pkg.settings.Settings

abstract class LaunchpadApiClient(
    private val distributionName: String,
    private val distributionSettings: Settings.Launchpad,
    communicationsSettings: Settings.Communications,
) : ApiClient<DistributionPackage>(
    communicationsSettings = communicationsSettings,
    defaultUrl = distributionSettings.url,
    json = {
        @OptIn(ExperimentalSerializationApi::class)
        namingStrategy = JsonNamingStrategy.SnakeCase
    }
) {
    private companion object {
        suspend inline fun <reified T> HttpClient.getCollection(
            urlString: String,
            noinline block: HttpRequestBuilder.() -> Unit = {},
        ): List<T> {
            var chunk: LaunchpadCollection<T> =
                get(urlString, block)
                    .body()
            val items: MutableList<T> =
                chunk.entries.toMutableList()
            while (chunk.nextCollectionLink != null) {
                chunk = get(chunk.nextCollectionLink)
                    .body()
                items.addAll(chunk.entries)
            }
            return items
        }
    }

    private val mutex = Mutex()

    private var distributionData: DistributionData? = null

    private suspend fun getDistributionData(): DistributionData =
        mutex.withLock {
            distributionData?.let { return it }

            val distribution: Distribution =
                httpClient.get(
                    urlString = distributionName
                )
                    .body()
            val releases: List<DistributionData.Release> =
                httpClient.getCollection<DistroSeries>(
                    urlString = distribution.seriesCollectionLink,
                )
                    .filter(DistroSeries::supported)
                    .map { distroSeries: DistroSeries ->
                        val distroArchSeries: DistroArchSeries =
                            httpClient.get(
                                urlString = distroSeries.selfLink
                            ) {
                                url {
                                    parameter("ws.op", "getDistroArchSeries")
                                    parameter("archtag", distributionSettings.architecture)
                                }
                            }
                                .body()
                        DistributionData.Release(
                            name = ReleaseName(distroSeries.name),
                            architectureLink = distroArchSeries.selfLink,
                        )
                    }
            distributionData = DistributionData(
                mainArchiveLink = distribution.mainArchiveLink,
                releases = releases,
            )
            checkNotNull(distributionData)
        }

    override suspend fun getReleases(): List<ReleaseName> =
        getDistributionData().releases
            .map(DistributionData.Release::name)

    override suspend fun getPackageVersions(
        distributionPackage: DistributionPackage,
    ): Map<ReleaseName, Version> {
        val distribution: DistributionData =
            getDistributionData()
        val binaryPackages: Map<String, List<BinaryPackagePublishingHistory>> =
            httpClient.getCollection<BinaryPackagePublishingHistory>(
                urlString = distribution.mainArchiveLink
            ) {
                parameter("ws.op", "getPublishedBinaries")
                parameter("status", "Published")
                parameter("binary_name", distributionPackage.name.value)
                parameter("exact_match", "true")
            }
                .groupBy(BinaryPackagePublishingHistory::distroArchSeriesLink)

        return distribution.releases
            .associateBy(
                DistributionData.Release::name
            ) { distributionRelease: DistributionData.Release ->
                binaryPackages.getOrDefault(
                    key = distributionRelease.architectureLink,
                    defaultValue = emptyList(),
                )
                    .map(BinaryPackagePublishingHistory::binaryPackageVersion)
            }
            .filterValues(List<String>::isNotEmpty)
            .mapValues { (_, value: List<String>) ->
                Version.fromString(
                    extractPackageVersion(value.first()),
                )
            }
    }

    protected abstract fun extractPackageVersion(
        value: String,
    ): String
}