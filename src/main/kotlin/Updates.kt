package pkg

class Updates(
    val releases: Releases,
    val packages: Packages,
) {
    class Releases(
        override val entries: Set<Map.Entry<Distribution, Map<ReleaseName, Map<PackageName, Version>>>>,
    ) : AbstractMap<Distribution, Map<ReleaseName, Map<PackageName, Version>>>() {
        constructor(
            from: Map<Distribution, List<ReleaseName>>,
            to: Map<Distribution, List<ReleaseName>>,
            packagesVersions: PackagesVersions,
        ) : this(
            associateWith(
                from,
                to
            ) { distribution: Distribution, fromReleases: List<ReleaseName>, toReleases: List<ReleaseName> ->
                (toReleases - fromReleases.toSet())
                    .associateWith { releaseName: ReleaseName ->
                        packagesVersions
                            .mapValues {
                                it.value[distribution]?.get(releaseName)
                            }
                            .filterValues {
                                it != null
                            }
                            .mapValues {
                                it.value!!
                            }
                    }
            }
                .filterValues(Map<ReleaseName, Map<PackageName, Version>>::isNotEmpty)
                .entries
        )
    }

    class Packages(
        from: PackagesVersions,
        to: PackagesVersions,
    ) : PackagesMap<Packages.Diff>(
        associateWith(from, to) { fromDistribution, toDistribution ->
            associateWith(fromDistribution, toDistribution) { fromRelease, toRelease ->
                associateWith(fromRelease, toRelease, ::Diff)
            }
                .filterValues(Map<ReleaseName, Diff>::any)
        }
            .filterValues {
                it.values.any { map ->
                    map.values.any(Diff::isChanged)
                }
            }
    ) {
        data class Diff(
            val from: Version,
            val to: Version,
        ) {
            val isChanged: Boolean =
                from != to
        }
    }
}

private fun <TKey, T, R> associateWith(
    from: Map<TKey, T>,
    to: Map<TKey, T>,
    block: (key: TKey, from: T, to: T) -> R,
): Map<TKey, R> =
    from.keys
        .intersect(to.keys)
        .associateWith { key: TKey ->
            block(
                key,
                from.getValue(key),
                to.getValue(key),
            )
        }

private fun <TKey, T, R> associateWith(
    from: Map<TKey, T>,
    to: Map<TKey, T>,
    block: (from: T, to: T) -> R,
): Map<TKey, R> =
    associateWith(from, to) { _: TKey, fromValue: T, toValue: T ->
        block(fromValue, toValue)
    }