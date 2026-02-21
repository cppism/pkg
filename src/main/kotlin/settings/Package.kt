package pkg.settings

class Package(
    val title: String,
    val arch: ArchDistributionPackage,
    val debian: DistributionPackage,
    val fedora: DistributionPackage,
    val ubuntu: DistributionPackage,
)