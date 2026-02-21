package pkg.distributions.debian

import kotlinx.datetime.*
import pkg.ReleaseName
import kotlin.time.Instant

internal object ReleasesIndex {
    val releases: List<Release> =
        listOf(
            Release(
                ReleaseName("trixie"),
                getInstant(2025, 8, 9),
                getInstant(2030, 6, 30),
            ),
            Release(
                ReleaseName("bookworm"),
                getInstant(2023, 6, 10),
                getInstant(2028, 6, 30),
            ),
            Release(
                ReleaseName("bullseye"),
                getInstant(2021, 8, 14),
                getInstant(2026, 8, 31),
            ),
        )

    private fun getInstant(
        year: Int,
        month: Int,
        day: Int,
    ): Instant =
        LocalDateTime(
            date = LocalDate(year, month, day),
            time = LocalTime(0, 0, 0, 0)
        ).toInstant(TimeZone.UTC)
}