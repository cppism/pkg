package pkg.notifications.telegram

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import pkg.notifications.telegram.api.SendMessageRequest
import pkg.notifications.telegram.api.SendMessageResponse
import pkg.notifications.telegram.api.TelegramMessage
import pkg.settings.Settings

class TelegramClient(
    token: String,
    notificationsSettings: Settings.Notifications.Telegram,
    communicationsSettings: Settings.Communications,
) : AutoCloseable {
    private val httpClient =
        HttpClient {
            expectSuccess = true
            defaultRequest {
                url("${notificationsSettings.url}bot$token/")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        @OptIn(ExperimentalSerializationApi::class)
                        namingStrategy = JsonNamingStrategy.SnakeCase
                    }
                )
            }
            install(Logging) {
                level = communicationsSettings.logLevel
            }
        }

    suspend fun sendMessage(
        message: TelegramMessage,
    ) {
        val response: SendMessageResponse =
            httpClient.post("sendMessage") {
                setBody(
                    SendMessageRequest(
                        chatId = message.channel,
                        text = message.text,
                        entities = message.entities,
                    )
                )
            }
                .body()
        check(response.ok)
    }

    override fun close() {
        httpClient.close()
    }
}