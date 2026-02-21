package pkg.notifications.telegram.api

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val chatId: String? = null,
    val text: String,
    val entities: List<MessageEntity> = emptyList(),
)