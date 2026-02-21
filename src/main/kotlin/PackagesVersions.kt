package pkg

import kotlinx.serialization.Serializable
import pkg.serialization.PackagesVersionsSerializer

@Serializable(with = PackagesVersionsSerializer::class)
class PackagesVersions(
    map: Map<PackageName, Value<Version>>,
) : PackagesMap<Version>(map)