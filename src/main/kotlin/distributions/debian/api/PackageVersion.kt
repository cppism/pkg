package pkg.distributions.debian.api

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageVersion(
    val version: String,
    val suites: List<String>,
)