package pkg

import pkg.distributions.arch.ArchApiClient
import pkg.distributions.debian.DebianApiClient
import pkg.distributions.fedora.FedoraApiClient
import pkg.distributions.launchpad.UbuntuApiClient
import pkg.settings.DistributionPackage
import pkg.settings.Package
import pkg.settings.Settings

class DistributionClientsMap(
    override val entries: Set<Map.Entry<Distribution, DistributionClient<out DistributionPackage>>>
) : AbstractMap<Distribution, DistributionClient<out DistributionPackage>>(), AutoCloseable {
    constructor(settings: Settings) :
            this(
                mapOf(
                    Distribution.ARCH to DistributionClient(
                        apiClient = ArchApiClient(
                            distributionSettings = settings.arch,
                            communicationsSettings = settings.communications,
                        ),
                        getDistributionPackage = Package::arch,
                    ),
                    Distribution.DEBIAN to DistributionClient(
                        apiClient = DebianApiClient(
                            distributionSettings = settings.debian,
                            communicationsSettings = settings.communications,
                        ),
                        getDistributionPackage = Package::debian,
                    ),
                    Distribution.FEDORA to DistributionClient(
                        apiClient = FedoraApiClient(
                            distributionSettings = settings.fedora,
                            communicationsSettings = settings.communications,
                        ),
                        getDistributionPackage = Package::fedora,
                    ),
                    Distribution.UBUNTU to DistributionClient(
                        apiClient = UbuntuApiClient(
                            distributionSettings = settings.launchpad,
                            communicationsSettings = settings.communications,
                        ),
                        getDistributionPackage = Package::ubuntu,
                    ),
                )
                    .entries
            )

    override fun close() {
        entries.forEach {
            it.value.close()
        }
    }
}