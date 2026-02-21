package pkg.distributions.debian.api

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageVersionsList(
    val versions: List<PackageVersion>,
)