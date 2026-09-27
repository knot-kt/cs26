package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.Notice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface PushPublisher {
    suspend fun publish(notice: Notice)

    companion object {
        fun fromEnvironment(): PushPublisher {
            val endpoint = System.getenv("CS26_NTFY_URL")?.trim()?.trimEnd('/')
            val topic = System.getenv("CS26_NTFY_TOPIC")?.trim()
            val token = System.getenv("CS26_NTFY_TOKEN")?.trim()
            return if (endpoint.isNullOrBlank() || topic.isNullOrBlank() || token.isNullOrBlank()) {
                NoopPushPublisher
            } else {
                NtfyPushPublisher(URI.create("$endpoint/$topic"), token)
            }
        }
    }
}

object NoopPushPublisher : PushPublisher {
    override suspend fun publish(notice: Notice) = Unit
}

class NtfyPushPublisher(
    private val endpoint: URI,
    private val token: String,
    private val client: HttpClient = HttpClient.newHttpClient(),
) : PushPublisher {
    override suspend fun publish(notice: Notice) {
        val request = HttpRequest.newBuilder(endpoint)
            .header("Authorization", "Bearer $token")
            .header("Title", notice.title)
            .header("Content-Type", "text/plain; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(notice.id))
            .build()
        withContext(Dispatchers.IO) {
            val response = client.send(request, HttpResponse.BodyHandlers.discarding())
            if (response.statusCode() !in 200..299) {
                throw IllegalStateException("push provider returned ${response.statusCode()}")
            }
        }
    }
}
