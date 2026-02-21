package pkg.settings

import pkg.PackageName

class ArchDistributionPackage(
    name: PackageName,
    val source: String,
) : DistributionPackage(
    name,
)