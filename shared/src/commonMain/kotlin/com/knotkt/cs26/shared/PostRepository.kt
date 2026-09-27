package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.CreatePostRequest
import com.knotkt.cs26.contracts.Post
import com.knotkt.cs26.contracts.PostPage
import com.knotkt.cs26.contracts.MediaUploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
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

    suspend fun create(accessToken: String, content: String, attachments: List<com.knotkt.cs26.contracts.MediaAttachment>): Post = client
        .post("$baseUrl/posts") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(com.knotkt.cs26.contracts.CreatePostRequest(content, attachments))
        }
        .body()

    suspend fun uploadMedia(accessToken: String, bytes: ByteArray, mimeType: String): MediaUploadResponse = client
        .submitFormWithBinaryData("$baseUrl/media/upload", formData {
            append("file", bytes, io.ktor.http.Headers.build {
                append(HttpHeaders.ContentType, mimeType)
                append(HttpHeaders.ContentDisposition, "filename=upload")
            })
        }) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun uploadImage(accessToken: String, bytes: ByteArray, mimeType: String): MediaUploadResponse =
        uploadMedia(accessToken, bytes, mimeType)
}
