package pkg.distributions.arch.api

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageData(
    val pkgver: String,
)