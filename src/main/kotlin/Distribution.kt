package pkg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Distribution(
    val title: String,
) {
    @SerialName("arch")
    ARCH("Arch"),

    @SerialName("debian")
    DEBIAN("Debian"),

    @SerialName("fedora")
    FEDORA("Fedora"),

    @SerialName("ubuntu")
    UBUNTU("Ubuntu"),
}