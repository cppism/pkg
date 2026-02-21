package pkg.settings

import io.ktor.client.plugins.logging.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Settings(
    val communications: Communications,
    val storage: Storage,
    val notifications: Notifications,
    val launchpad: Launchpad,
    val arch: Arch,
    val debian: Debian,
    val fedora: Fedora,
) {
    @Serializable
    data class Communications(
        val logLevel: LogLevel,
        val requestTimeout: Duration,
    )

    @Serializable
    data class Storage(
        val path: String,
        val packagesDirectory: String,
        val packagesFile: String,
        val messagesDirectory: String,
    )

    @Serializable
    data class Notifications(
        val telegram: Telegram,
    ) {


        @Serializable
        data class Telegram(
            val url: String,
            val channels: List<String>,
        )
    }

    @Serializable
    data class Launchpad(
        val url: String,
        val architecture: String,
    )

    @Serializable
    data class Arch(
        val url: String,
        val architecture: String,
    )

    @Serializable
    data class Debian(
        val url: String,
    )

    @Serializable
    data class Fedora(
        val url: String,
        val releaseNamePattern: String,
    )
}