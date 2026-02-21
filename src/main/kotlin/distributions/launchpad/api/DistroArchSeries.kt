package pkg.distributions.launchpad.api

import kotlinx.serialization.Serializable

@Serializable
internal data class DistroArchSeries(
    val selfLink: String,
)