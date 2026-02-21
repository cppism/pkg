package pkg.distributions.launchpad

import pkg.settings.Settings

class UbuntuApiClient(
    distributionSettings: Settings.Launchpad,
    communicationsSettings: Settings.Communications,
) : LaunchpadApiClient(
    distributionName = "ubuntu",
    distributionSettings = distributionSettings,
    communicationsSettings = communicationsSettings,
) {
    override fun extractPackageVersion(
        value: String,
    ): String =
        value
            .substringBefore('-')
            .substringAfter(':')
}