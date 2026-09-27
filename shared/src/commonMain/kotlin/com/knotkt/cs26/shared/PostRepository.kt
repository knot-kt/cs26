package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.CreatePostRequest
import com.knotkt.cs26.contracts.Post
import com.knotkt.cs26.contracts.PostPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class PostRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun list(accessToken: String): List<Post> = client
        .get("$baseUrl/posts") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body<PostPage>()
        .items

    suspend fun create(accessToken: String, content: String): Post = client
        .post("$baseUrl/posts") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(CreatePostRequest(content))
        }
        .body()
}