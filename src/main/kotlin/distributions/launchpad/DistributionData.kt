package pkg.distributions.launchpad

import pkg.ReleaseName

data class DistributionData(
    val mainArchiveLink: String,
    val releases: List<Release>,
) {
    data class Release(
        val name: ReleaseName,
        val architectureLink: String,
    )
}