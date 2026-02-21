package pkg.distributions.fedora.api

import kotlinx.serialization.Serializable

@Serializable
data class PackageInfo(
    val version: String,
)