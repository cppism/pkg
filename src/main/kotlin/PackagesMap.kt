package pkg

abstract class PackagesMap<T>(
    override val entries: Set<Map.Entry<PackageName, Value<T>>>
) : AbstractMap<PackageName, PackagesMap.Value<T>>() {
    protected typealias Value<T> = Map<Distribution, Map<ReleaseName, T>>

    constructor(map: Map<PackageName, Value<T>>) :
            this(map.entries)
}