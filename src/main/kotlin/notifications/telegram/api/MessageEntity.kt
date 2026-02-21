package pkg.notifications.telegram.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageEntity(
    val type: Type,
    val offset: Int,
    val length: Int,
) {
    enum class Type {
        @SerialName("bold")
        BOLD,

        @SerialName("hashtag")
        HASHTAG,
    }
}