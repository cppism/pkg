package pkg.notifications.telegram.api

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    override val ok: Boolean,
) : Response