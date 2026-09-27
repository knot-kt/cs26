package com.knotkt.cs26.android

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.knotkt.cs26.shared.Cs26App
import com.knotkt.cs26.shared.AuthRepository
import com.knotkt.cs26.shared.AuthState
import com.knotkt.cs26.shared.HealthRepository
import com.knotkt.cs26.shared.HealthState
import com.knotkt.cs26.shared.PostRepository
import com.knotkt.cs26.shared.PostState
import com.knotkt.cs26.shared.ChatRepository
import com.knotkt.cs26.shared.ChatState
import com.knotkt.cs26.shared.NoticeRepository
import com.knotkt.cs26.shared.NoticeState
import com.knotkt.cs26.contracts.ChatMessage
import com.knotkt.cs26.contracts.SendMessageRequest
import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.MediaKind
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.UUID
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(WebSockets)
    }
    private val healthRepository = HealthRepository(client, "http://10.0.2.2:8080")
    private val authRepository = AuthRepository(client, "http://10.0.2.2:8080")
    private val postRepository = PostRepository(client, "http://10.0.2.2:8080")
    private val chatRepository = ChatRepository(client, "http://10.0.2.2:8080")
    private val noticeRepository = NoticeRepository(client, "http://10.0.2.2:8080")
    private var healthState by mutableStateOf(HealthState())
    private var authState by mutableStateOf(AuthState())
    private var postState by mutableStateOf(PostState())
    private var chatState by mutableStateOf(ChatState())
    private var noticeState by mutableStateOf(NoticeState())
    private var chatJob: Job? = null
    private var chatSession: io.ktor.client.plugins.websocket.DefaultClientWebSocketSession? = null
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uploadImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs26App(
                healthState = healthState,
                onCheckHealth = ::checkHealth,
                authState = authState,
                onPhoneChanged = { phone -> authState = authState.copy(phone = phone, error = null) },
                onCodeChanged = { code -> authState = authState.copy(code = code, error = null) },
                onRequestCode = ::requestCode,
                onVerifyCode = ::verifyCode,
                onLogout = ::logout,
                postState = postState,
                onPostContentChanged = { content -> postState = postState.copy(content = content, error = null) },
                onPublishPost = ::publishPost,
                onRefreshPosts = ::refreshPosts,
                onAddImage = { imagePicker.launch("image/*") },
                chatState = chatState,
                onChatInputChanged = { input -> chatState = chatState.copy(input = input, error = null) },
                onConnectChat = ::connectChat,
                onSendChat = ::sendChat,
                noticeState = noticeState,
                onRefreshNotices = ::refreshNotices,
                onMarkNoticeRead = ::markNoticeRead,
            )
        }
    }

    private fun checkHealth() {
        healthState = healthState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { healthRepository.check() }
            }
            healthState = result.fold(
                onSuccess = { response -> HealthState(response = response) },
                onFailure = { error -> HealthState(error = error.message ?: "request failed") },
            )
        }
    }

    private fun requestCode() {
        val phone = authState.phone.trim()
        authState = authState.copy(isRequestingCode = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { authRepository.requestCode(phone) }
            }
            authState = result.fold(
                onSuccess = { response ->
                    authState.copy(
                        code = response.developmentCode.orEmpty(),
                        expiresInSeconds = response.expiresInSeconds,
                        developmentCode = response.developmentCode,
                        isRequestingCode = false,
                    )
                },
                onFailure = { error ->
                    authState.copy(
                        isRequestingCode = false,
                        error = error.message ?: "request code failed",
                    )
                },
            )
        }
    }

    private fun verifyCode() {
        val phone = authState.phone.trim()
        val code = authState.code.trim()
        authState = authState.copy(isVerifyingCode = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { authRepository.verifyCode(phone, code) }
            }
            authState = result.fold(
                onSuccess = { session ->
                    authState.copy(session = session, isVerifyingCode = false)
                },
                onFailure = { error ->
                    authState.copy(
                        isVerifyingCode = false,
                        error = error.message ?: "verify code failed",
                    )
                },
            )
            if (result.isSuccess) refreshPosts()
            if (result.isSuccess) refreshNotices()
        }
    }

    private fun refreshPosts() {
        val session = authState.session ?: return
        postState = postState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.list(session.accessToken) }
            }
            postState = result.fold(
                onSuccess = { posts -> postState.copy(posts = posts, isLoading = false) },
                onFailure = { error -> postState.copy(isLoading = false, error = error.message ?: "load posts failed") },
            )
        }
    }

    private fun connectChat() {
        val session = authState.session ?: return
        chatJob?.cancel()
        chatState = chatState.copy(isConnecting = true, error = null)
        chatJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val history = chatRepository.history(session.accessToken, chatState.conversationId)
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(messages = history, isConnecting = false, error = null)
                    }
                    client.webSocket(
                        urlString = "ws://10.0.2.2:8080/conversations/${chatState.conversationId}/stream",
                        request = { header(io.ktor.http.HttpHeaders.Authorization, "Bearer ${session.accessToken}") },
                    ) {
                        chatSession = this
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val message = Json.decodeFromString<ChatMessage>(frame.readText())
                                withContext(Dispatchers.Main) {
                                    if (chatState.messages.none { it.id == message.id }) {
                                        chatState = chatState.copy(messages = chatState.messages + message)
                                    }
                                }
                            }
                        }
                    }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(isConnecting = false, error = error.message ?: "chat disconnected")
                    }
                } finally {
                    chatSession = null
                }
                delay(1_000)
            }
        }
    }

    private fun sendChat() {
        val session = chatSession ?: run {
            connectChat()
            return
        }
        val input = chatState.input.trim()
        if (input.isBlank()) return
        val request = SendMessageRequest(UUID.randomUUID().toString(), input)
        chatState = chatState.copy(input = "")
        scope.launch(Dispatchers.IO) {
            runCatching { session.send(Frame.Text(Json.encodeToString(request))) }
                .onFailure { error ->
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(input = input, error = error.message ?: "send failed")
                    }
                }
        }
    }

    private fun refreshNotices() {
        val session = authState.session ?: return
        noticeState = noticeState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { noticeRepository.list(session.accessToken) }
            }
            noticeState = result.fold(
                onSuccess = { notices -> noticeState.copy(notices = notices, isLoading = false) },
                onFailure = { error -> noticeState.copy(isLoading = false, error = error.message ?: "load notifications failed") },
            )
        }
    }

    private fun markNoticeRead(noticeId: String) {
        val session = authState.session ?: return
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { noticeRepository.markRead(session.accessToken, noticeId) }
            }
            if (result.isSuccess) refreshNotices()
            else noticeState = noticeState.copy(error = result.exceptionOrNull()?.message ?: "mark notification failed")
        }
    }

    private fun publishPost() {
        val session = authState.session ?: return
        val content = postState.content.trim()
        postState = postState.copy(isPublishing = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.create(session.accessToken, content, postState.attachments) }
            }
            postState = result.fold(
                onSuccess = { post -> postState.copy(content = "", posts = listOf(post) + postState.posts, isPublishing = false) },
                onFailure = { error -> postState.copy(isPublishing = false, error = error.message ?: "publish post failed") },
            )
        }
    }

    private fun uploadImage(uri: android.net.Uri) {
        val session = authState.session ?: return
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        postState = postState.copy(isUploading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.uploadImage(session.accessToken, bytes, mimeType) }
            }
            postState = result.fold(
                onSuccess = { uploaded ->
                    postState.copy(
                        attachments = postState.attachments + MediaAttachment(
                            kind = MediaKind.IMAGE,
                            objectKey = uploaded.objectKey,
                            mimeType = uploaded.mimeType,
                            sizeBytes = uploaded.sizeBytes,
                        ),
                        isUploading = false,
                    )
                },
                onFailure = { error -> postState.copy(isUploading = false, error = error.message ?: "upload failed") },
            )
        }
    }

    private fun logout() {
        val session = authState.session ?: return
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { authRepository.logout(session.accessToken) }
            }
            authState = AuthState(phone = authState.phone)
            postState = PostState()
            chatJob?.cancel()
            chatSession = null
            chatState = ChatState()
            noticeState = NoticeState()
        }
    }

    override fun onDestroy() {
        client.close()
        scope.cancel()
        super.onDestroy()
    }
}