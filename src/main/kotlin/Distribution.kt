package pkg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Distribution {
    @SerialName("arch")
    ARCH,

    @SerialName("debian")
    DEBIAN,

    @SerialName("fedora")
    FEDORA,

    @SerialName("ubuntu")
    UBUNTU,
}