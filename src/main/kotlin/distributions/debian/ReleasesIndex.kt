package pkg.distributions.debian

import kotlinx.datetime.*
import pkg.ReleaseName
import kotlin.time.Instant

internal object ReleasesIndex {
    val releases: List<Suite> =
        listOf(
            Suite(
                releaseName = ReleaseName("trixie"),
                sinceDate = getInstant(2025, 8, 9),
                dueDate = getInstant(2030, 6, 30),
            ),
            Suite(
                releaseName = ReleaseName("bookworm"),
                sinceDate = getInstant(2023, 6, 10),
                dueDate = getInstant(2028, 6, 30),
            ),
            Suite(
                releaseName = ReleaseName("bullseye"),
                sinceDate = getInstant(2021, 8, 14),
                dueDate = getInstant(2026, 8, 31),
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