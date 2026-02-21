package pkg

import pkg.distributions.ApiClient
import pkg.settings.DistributionPackage
import pkg.settings.Package

class DistributionClient<TPackage : DistributionPackage>(
    private val apiClient: ApiClient<TPackage>,
    private val getDistributionPackage: (Package) -> TPackage,
) : AutoCloseable {
    suspend fun getPackageVersions(
        pkg: Package,
    ): Map<ReleaseName, Version> =
        apiClient.getPackageVersions(
            getDistributionPackage(pkg)
        )

    override fun close() {
        apiClient.close()
    }
}