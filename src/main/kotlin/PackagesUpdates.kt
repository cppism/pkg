package pkg

class PackagesUpdates(
    from: PackagesVersions,
    to: PackagesVersions,
) : PackagesMap<PackagesUpdates.Diff>(
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

    private companion object {
        fun <TKey, T, R> associateWith(
            from: Map<TKey, T>,
            to: Map<TKey, T>,
            block: (from: T, to: T) -> R,
        ): Map<TKey, R> =
            from.keys
                .intersect(to.keys)
                .associateWith { key: TKey ->
                    block(
                        from.getValue(key),
                        to.getValue(key),
                    )
                }
    }
}