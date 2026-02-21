package pkg.distributions.launchpad.api

import kotlinx.serialization.Serializable

@Serializable
internal data class BinaryPackagePublishingHistory(
    val binaryPackageVersion: String,
    val distroArchSeriesLink: String,
)