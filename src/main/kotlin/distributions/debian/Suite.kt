package pkg.distributions.debian

import pkg.ReleaseName
import kotlin.time.Clock
import kotlin.time.Instant

internal data class Suite(
    val releaseName: ReleaseName,
    val sinceDate: Instant,
    val dueDate: Instant,
) {
    val isSupported: Boolean
        get() =
            Clock.System.now() in sinceDate..dueDate
}