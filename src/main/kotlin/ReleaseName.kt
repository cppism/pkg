package pkg

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ReleaseName(
    val value: String,
) {
    override fun toString(): String =
        value
}