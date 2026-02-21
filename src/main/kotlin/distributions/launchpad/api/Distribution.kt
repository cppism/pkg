package pkg.distributions.launchpad.api

import kotlinx.serialization.Serializable

@Serializable
internal data class Distribution(
    val mainArchiveLink: String,
    val seriesCollectionLink: String,
)