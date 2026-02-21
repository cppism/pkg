package pkg

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class PackageName(
    val value: String,
) {
    override fun toString(): String =
        value
}