package pkg.distributions.launchpad

import kotlinx.serialization.Serializable

@Serializable
internal data class LaunchpadCollection<T>(
    val nextCollectionLink: String? = null,
    val entries: List<T>,
)