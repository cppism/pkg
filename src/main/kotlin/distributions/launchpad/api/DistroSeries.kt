package pkg.distributions.launchpad.api

import kotlinx.serialization.Serializable

@Serializable
internal data class DistroSeries(
    val name: String,
    val selfLink: String,
    val supported: Boolean,
)