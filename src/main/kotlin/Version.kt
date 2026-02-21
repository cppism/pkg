package pkg

import kotlinx.serialization.Serializable
import pkg.serialization.VersionSerializer

@Serializable(VersionSerializer::class)
data class Version(
    val major: UInt,
    val minor: UInt,
    val patch: String? = null,
) {
    companion object {
        fun fromString(value: String): Version {
            require(value.isNotEmpty())
            val split: List<String> =
                value.split('.')
            check(split.size in 2..5)
            return Version(
                major = split[0].toUInt(),
                minor = split[1].toUInt(),
                patch = split.getOrNull(2),
            )
        }
    }

    override fun toString(): String =
        if (patch == null)
            "$major.$minor"
        else
            "$major.$minor.$patch"
}