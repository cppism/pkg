package pkg.distributions

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import pkg.ReleaseName
import pkg.Version
import pkg.settings.DistributionPackage
import pkg.settings.Settings

abstract class ApiClient<TPackage : DistributionPackage>(
    communicationsSettings: Settings.Communications,
    defaultUrl: String,
    json: JsonBuilder.() -> Unit = {},
) : AutoCloseable {
    protected val httpClient =
        HttpClient {
            expectSuccess = true
            followRedirects = false
            defaultRequest {
                url(defaultUrl)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        json()
                    }
                )
            }
            install(HttpRequestRetry) {
                retryOnException(
                    maxRetries = 2,
                    retryOnTimeout = true,
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis =
                    communicationsSettings.requestTimeout.inWholeMilliseconds
            }
            install(Logging) {
                level = communicationsSettings.logLevel
            }
        }

    abstract suspend fun getPackageVersions(
        distributionPackage: TPackage,
    ): Map<ReleaseName, Version>

    override fun close() {
        httpClient.close()
    }
}