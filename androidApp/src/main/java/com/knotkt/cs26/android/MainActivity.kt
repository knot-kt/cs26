package com.knotkt.cs26.android

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Build
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
import com.knotkt.cs26.shared.ProfileRepository
import com.knotkt.cs26.shared.ProfileState
import com.knotkt.cs26.shared.FollowRepository
import com.knotkt.cs26.shared.FollowState
import com.knotkt.cs26.shared.SafetyRepository
import com.knotkt.cs26.shared.SafetyState
import com.knotkt.cs26.contracts.ChatStreamEvent
import com.knotkt.cs26.contracts.SendMessageRequest
import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.MediaKind
import com.knotkt.cs26.contracts.PostType
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
import java.io.File
import java.io.ByteArrayOutputStream
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
    private val baseUrl = BuildConfig.CS26_BASE_URL
    private val healthRepository = HealthRepository(client, baseUrl)
    private val authRepository = AuthRepository(client, baseUrl)
    private val postRepository = PostRepository(client, baseUrl)
    private val chatRepository = ChatRepository(client, baseUrl)
    private val noticeRepository = NoticeRepository(client, baseUrl)
    private val profileRepository = ProfileRepository(client, baseUrl)
    private val followRepository = FollowRepository(client, baseUrl)
    private val safetyRepository = SafetyRepository(client, baseUrl)
    private var healthState by mutableStateOf(HealthState())
    private var authState by mutableStateOf(AuthState())
    private var postState by mutableStateOf(PostState())
    private var chatState by mutableStateOf(ChatState())
    private var noticeState by mutableStateOf(NoticeState())
    private var profileState by mutableStateOf(ProfileState())
    private var followState by mutableStateOf(FollowState())
    private var safetyState by mutableStateOf(SafetyState())
    private var chatJob: Job? = null
    private var chatSession: io.ktor.client.plugins.websocket.DefaultClientWebSocketSession? = null
    private var noticeJob: Job? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var audioPlayer: MediaPlayer? = null
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uploadImage(uri)
    }
    private val cameraPicker = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) uploadCapturedImage(bitmap)
    }
    private val chatImagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) uploadChatImage(uri)
    }
    private val audioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecordingInternal()
        else chatState = chatState.copy(error = "Microphone permission is required to record audio")
    }
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraPicker.launch(null)
        else postState = postState.copy(error = "Camera permission is required to take a photo")
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
                onPostTypeChanged = { type -> postState = postState.copy(type = type, error = null) },
                onPublishPost = ::publishPost,
                onRefreshPosts = ::refreshPosts,
                onLoadMorePosts = ::loadMorePosts,
                onAddImage = { imagePicker.launch("image/*") },
                onTakePhoto = ::takePhoto,
                onToggleLike = ::toggleLike,
                onDeletePost = ::deletePost,
                onLoadComments = ::loadComments,
                onCommentInputChanged = { postId, input ->
                    postState = postState.copy(commentInputs = postState.commentInputs + (postId to input), error = null)
                },
                onAddComment = ::addComment,
                chatState = chatState,
                onChatInputChanged = { input -> chatState = chatState.copy(input = input, error = null) },
                onConnectChat = ::connectChat,
                onSendChat = ::sendChat,
                onAddChatImage = { chatImagePicker.launch("image/*") },
                onPlayAudio = ::playAudio,
                onStartRecording = ::startRecording,
                onStopRecording = ::stopRecording,
                noticeState = noticeState,
                onRefreshNotices = ::refreshNotices,
                onMarkNoticeRead = ::markNoticeRead,
                profileState = profileState,
                onProfileNicknameChanged = { nickname -> profileState = profileState.copy(nickname = nickname, error = null) },
                onProfileInterestsChanged = { interests -> profileState = profileState.copy(interestsInput = interests, error = null) },
                onProfileAnonymousChanged = { anonymous -> profileState = profileState.copy(anonymousByDefault = anonymous, error = null) },
                onRefreshProfile = ::refreshProfile,
                onSaveProfile = ::saveProfile,
                followState = followState,
                onToggleFollow = ::toggleFollow,
                safetyState = safetyState,
                onReportPost = ::reportPost,
                onBlockUser = ::blockUser,
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
            if (result.isSuccess) {
                refreshProfile()
                refreshFollowing()
                refreshSafety()
                refreshNotices()
                connectNotices()
            }
        }
    }

    private fun refreshPosts() {
        val session = authState.session ?: return
        postState = postState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.listPage(session.accessToken) }
            }
            postState = result.fold(
                onSuccess = { page -> postState.copy(posts = page.items, nextCursor = page.nextCursor, isLoading = false) },
                onFailure = { error -> postState.copy(isLoading = false, error = error.message ?: "load posts failed") },
            )
        }
    }

    private fun loadMorePosts() {
        val session = authState.session ?: return
        val cursor = postState.nextCursor ?: return
        if (postState.isLoadingMore) return
        postState = postState.copy(isLoadingMore = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.listPage(session.accessToken, cursor) }
            }
            postState = result.fold(
                onSuccess = { page ->
                    val knownIds = postState.posts.mapTo(mutableSetOf()) { it.id }
                    postState.copy(
                        posts = postState.posts + page.items.filterNot { it.id in knownIds },
                        nextCursor = page.nextCursor,
                        isLoadingMore = false,
                    )
                },
                onFailure = { error ->
                    postState.copy(isLoadingMore = false, error = error.message ?: "load more posts failed")
                },
            )
        }
    }

    private fun toggleLike(postId: String) {
        val session = authState.session ?: return
        postState = postState.copy(isInteracting = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.toggleLike(session.accessToken, postId) }
            }
            if (result.isSuccess) refreshPosts()
            postState = postState.copy(
                isInteracting = false,
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    private fun deletePost(postId: String) {
        val session = authState.session ?: return
        postState = postState.copy(isInteracting = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.delete(session.accessToken, postId) }
            }
            if (result.isSuccess) refreshPosts()
            postState = postState.copy(
                isInteracting = false,
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    private fun loadComments(postId: String) {
        val session = authState.session ?: return
        postState = postState.copy(isInteracting = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.comments(session.accessToken, postId) }
            }
            postState = result.fold(
                onSuccess = { comments ->
                    postState.copy(
                        isInteracting = false,
                        commentsByPost = postState.commentsByPost + (postId to comments),
                    )
                },
                onFailure = { error -> postState.copy(isInteracting = false, error = error.message ?: "load comments failed") },
            )
        }
    }

    private fun addComment(postId: String) {
        val session = authState.session ?: return
        val content = postState.commentInputs[postId].orEmpty().trim()
        if (content.isBlank()) return
        postState = postState.copy(isInteracting = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.addComment(session.accessToken, postId, content) }
            }
            if (result.isSuccess) {
                val comment = result.getOrThrow()
                postState = postState.copy(
                    isInteracting = false,
                    commentsByPost = postState.commentsByPost +
                        (postId to (postState.commentsByPost[postId].orEmpty() + comment)),
                    commentInputs = postState.commentInputs - postId,
                )
                refreshPosts()
            } else {
                postState = postState.copy(isInteracting = false, error = result.exceptionOrNull()?.message ?: "add comment failed")
            }
        }
    }

    private fun connectChat() {
        val session = authState.session ?: return
        chatJob?.cancel()
        chatState = chatState.copy(isConnecting = true, isConnected = false, error = null)
        chatJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                withContext(Dispatchers.Main) {
                    chatState = chatState.copy(isConnecting = true, isConnected = false)
                }
                try {
                    val history = chatRepository.history(session.accessToken, chatState.conversationId)
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(
                            messages = history,
                            isConnecting = false,
                            isConnected = true,
                            error = null,
                        )
                    }
                    client.webSocket(
                        urlString = baseUrl.replaceFirst("http", "ws") +
                            "/conversations/${chatState.conversationId}/stream",
                        request = { header(io.ktor.http.HttpHeaders.Authorization, "Bearer ${session.accessToken}") },
                    ) {
                        chatSession = this
                        val pending = withContext(Dispatchers.Main) { chatState.pendingMessages.toList() }
                        pending.forEach { request ->
                            send(Frame.Text(Json.encodeToString(request)))
                        }
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                when (val event = Json.decodeFromString<ChatStreamEvent>(frame.readText())) {
                                    is ChatStreamEvent.Message -> withContext(Dispatchers.Main) {
                                        val message = event.message
                                        if (chatState.messages.none { it.id == message.id }) {
                                            chatState = chatState.copy(messages = chatState.messages + message)
                                        }
                                    }
                                    is ChatStreamEvent.Receipt -> withContext(Dispatchers.Main) {
                                        chatState = chatState.copy(
                                            pendingMessages = chatState.pendingMessages.filterNot {
                                                it.clientMessageId == event.clientMessageId
                                            },
                                            lastDeliveryStatus = event.status.name.lowercase(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(
                            isConnecting = isActive,
                            isConnected = false,
                            error = error.message ?: "chat disconnected",
                        )
                    }
                } finally {
                    chatSession = null
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(isConnected = false, isConnecting = isActive)
                    }
                }
                delay(1_000)
            }
        }
    }

    private fun sendChat() {
        val input = chatState.input.trim()
        if (input.isBlank() && chatState.attachments.isEmpty()) return
        val request = SendMessageRequest(UUID.randomUUID().toString(), input, chatState.attachments)
        chatState = chatState.copy(
            input = "",
            attachments = emptyList(),
            pendingMessages = chatState.pendingMessages + request,
            error = null,
        )
        val session = chatSession ?: run {
            connectChat()
            return
        }
        sendPendingMessage(session, request)
    }

    private fun sendPendingMessage(
        session: io.ktor.client.plugins.websocket.DefaultClientWebSocketSession,
        request: SendMessageRequest,
    ) {
        scope.launch(Dispatchers.IO) {
            runCatching { session.send(Frame.Text(Json.encodeToString(request))) }
                .onFailure { error ->
                    withContext(Dispatchers.Main) {
                        chatState = chatState.copy(
                            error = error.message ?: "send failed",
                        )
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

    private fun refreshProfile() {
        val session = authState.session ?: return
        profileState = profileState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { profileRepository.get(session.accessToken) }
            }
            profileState = result.fold(
                onSuccess = { profile ->
                    profileState.copy(
                        profile = profile,
                        nickname = profile.nickname,
                        interestsInput = profile.interests.joinToString(", "),
                        anonymousByDefault = profile.anonymousByDefault,
                        isLoading = false,
                    )
                },
                onFailure = { error -> profileState.copy(isLoading = false, error = error.message ?: "load profile failed") },
            )
        }
    }

    private fun saveProfile() {
        val session = authState.session ?: return
        val interests = profileState.interestsInput
            .split(",", "，", "#")
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
        profileState = profileState.copy(isSaving = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    profileRepository.update(
                        session.accessToken,
                        profileState.nickname,
                        interests,
                        profileState.anonymousByDefault,
                    )
                }
            }
            profileState = result.fold(
                onSuccess = { profile ->
                    profileState.copy(
                        profile = profile,
                        nickname = profile.nickname,
                        interestsInput = profile.interests.joinToString(", "),
                        anonymousByDefault = profile.anonymousByDefault,
                        isSaving = false,
                    )
                },
                onFailure = { error -> profileState.copy(isSaving = false, error = error.message ?: "save profile failed") },
            )
        }
    }

    private fun refreshFollowing() {
        val session = authState.session ?: return
        followState = followState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { followRepository.list(session.accessToken) }
            }
            followState = result.fold(
                onSuccess = { following -> followState.copy(followingIds = following.userIds.toSet(), isLoading = false) },
                onFailure = { error -> followState.copy(isLoading = false, error = error.message ?: "load following failed") },
            )
        }
    }

    private fun toggleFollow(userId: String) {
        val session = authState.session ?: return
        val isFollowing = userId in followState.followingIds
        followState = followState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    if (isFollowing) followRepository.unfollow(session.accessToken, userId)
                    else followRepository.follow(session.accessToken, userId)
                }
            }
            followState = result.fold(
                onSuccess = { response ->
                    followState.copy(
                        followingIds = if (response.following) {
                            followState.followingIds + userId
                        } else {
                            followState.followingIds - userId
                        },
                        isLoading = false,
                    )
                },
                onFailure = { error -> followState.copy(isLoading = false, error = error.message ?: "update follow failed") },
            )
        }
    }

    private fun refreshSafety() {
        val session = authState.session ?: return
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { safetyRepository.listBlocked(session.accessToken) }
            }
            result.onSuccess { blocked ->
                safetyState = safetyState.copy(blockedUserIds = blocked.userIds.toSet())
            }.onFailure { error ->
                safetyState = safetyState.copy(error = error.message ?: "load blocked users failed")
            }
        }
    }

    private fun reportPost(postId: String) {
        val session = authState.session ?: return
        safetyState = safetyState.copy(isUpdating = true, message = null, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { safetyRepository.reportPost(session.accessToken, postId) }
            }
            safetyState = result.fold(
                onSuccess = { receipt -> safetyState.copy(isUpdating = false, message = "举报已提交（${receipt.reportId.take(8)}）") },
                onFailure = { error -> safetyState.copy(isUpdating = false, error = error.message ?: "report failed") },
            )
        }
    }

    private fun blockUser(userId: String) {
        val session = authState.session ?: return
        safetyState = safetyState.copy(isUpdating = true, message = null, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { safetyRepository.block(session.accessToken, userId) }
            }
            safetyState = result.fold(
                onSuccess = { response ->
                    safetyState.copy(
                        blockedUserIds = if (response.blocked) safetyState.blockedUserIds + userId else safetyState.blockedUserIds - userId,
                        isUpdating = false,
                        message = "已屏蔽该成员",
                    )
                },
                onFailure = { error -> safetyState.copy(isUpdating = false, error = error.message ?: "block failed") },
            )
            if (result.isSuccess) refreshPosts()
        }
    }

    private fun connectNotices() {
        val session = authState.session ?: return
        noticeJob?.cancel()
        noticeState = noticeState.copy(isConnecting = true, error = null)
        noticeJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    client.webSocket(
                        urlString = baseUrl.replaceFirst("http", "ws") + "/notifications/stream",
                        request = { header(io.ktor.http.HttpHeaders.Authorization, "Bearer ${session.accessToken}") },
                    ) {
                        withContext(Dispatchers.Main) {
                            noticeState = noticeState.copy(isConnecting = true, error = null)
                        }
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val notice = Json.decodeFromString<com.knotkt.cs26.contracts.Notice>(frame.readText())
                                withContext(Dispatchers.Main) {
                                    val existing = noticeState.notices.firstOrNull { it.id == notice.id }
                                    noticeState = noticeState.copy(
                                        notices = listOf(notice.copy(read = existing?.read ?: notice.read)) +
                                            noticeState.notices.filterNot { it.id == notice.id },
                                    )
                                }
                            }
                        }
                    }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) {
                        noticeState = noticeState.copy(isConnecting = false, error = error.message ?: "notification stream disconnected")
                    }
                }
                delay(1_000)
            }
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
                withContext(Dispatchers.IO) {
                    postRepository.create(session.accessToken, content, postState.attachments, postState.type)
                }
            }
            postState = result.fold(
                onSuccess = {
                    post -> postState.copy(content = "", type = PostType.GENERAL, posts = listOf(post) + postState.posts, isPublishing = false)
                },
                onFailure = { error -> postState.copy(isPublishing = false, error = error.message ?: "publish post failed") },
            )
        }
    }

    private fun uploadImage(uri: android.net.Uri) {
        if (authState.session == null) return
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        uploadPostImage(bytes, mimeType)
    }

    private fun takePhoto() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        cameraPicker.launch(null)
    }

    private fun uploadCapturedImage(bitmap: Bitmap) {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        uploadPostImage(output.toByteArray(), "image/jpeg")
    }

    private fun uploadPostImage(bytes: ByteArray, mimeType: String) {
        val session = authState.session ?: return
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

    private fun uploadChatImage(uri: android.net.Uri) {
        val session = authState.session ?: return
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        chatState = chatState.copy(isUploading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { postRepository.uploadMedia(session.accessToken, bytes, mimeType) }
            }
            chatState = result.fold(
                onSuccess = { uploaded ->
                    chatState.copy(
                        attachments = chatState.attachments + MediaAttachment(
                            kind = MediaKind.IMAGE,
                            objectKey = uploaded.objectKey,
                            mimeType = uploaded.mimeType,
                            sizeBytes = uploaded.sizeBytes,
                        ),
                        isUploading = false,
                    )
                },
                onFailure = { error -> chatState.copy(isUploading = false, error = error.message ?: "chat image upload failed") },
            )
        }
    }

    private fun startRecording() {
        if (mediaRecorder != null) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        startRecordingInternal()
    }

    private fun startRecordingInternal() {
        if (mediaRecorder != null) return
        val file = File.createTempFile("cs26-chat-", ".m4a", cacheDir)
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recordingFile = file
        mediaRecorder = recorder
        chatState = chatState.copy(isRecording = true, error = null)
    }

    private fun stopRecording() {
        val recorder = mediaRecorder ?: return
        val file = recordingFile
        mediaRecorder = null
        recordingFile = null
        chatState = chatState.copy(isRecording = false)
        runCatching { recorder.stop() }
            .onFailure { error ->
                recorder.release()
                file?.delete()
                chatState = chatState.copy(error = error.message ?: "recording was too short")
            }
            .onSuccess {
                recorder.release()
                if (file != null) uploadChatAudio(file)
            }
    }

    private fun uploadChatAudio(file: File) {
        val session = authState.session ?: return
        chatState = chatState.copy(isUploading = true, error = null)
        scope.launch {
            val result = runCatching {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                withContext(Dispatchers.IO) { postRepository.uploadMedia(session.accessToken, bytes, "audio/mp4") }
            }
            file.delete()
            chatState = result.fold(
                onSuccess = { uploaded ->
                    chatState.copy(
                        attachments = chatState.attachments + MediaAttachment(
                            kind = MediaKind.AUDIO,
                            objectKey = uploaded.objectKey,
                            mimeType = uploaded.mimeType,
                            sizeBytes = uploaded.sizeBytes,
                        ),
                        isUploading = false,
                    )
                },
                onFailure = { error -> chatState.copy(isUploading = false, error = error.message ?: "audio upload failed") },
            )
        }
    }

    private fun playAudio(objectKey: String) {
        val session = authState.session ?: return
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bytes = postRepository.downloadMedia(session.accessToken, objectKey)
                    File.createTempFile("cs26-playback-", ".m4a", cacheDir).also { it.writeBytes(bytes) }
                }
            }
            result.onSuccess { file ->
                audioPlayer?.release()
                audioPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    setOnCompletionListener {
                        release()
                        if (audioPlayer === this) audioPlayer = null
                        file.delete()
                    }
                    setOnErrorListener { player, _, _ ->
                        player.release()
                        if (audioPlayer === player) audioPlayer = null
                        file.delete()
                        true
                    }
                    prepare()
                    start()
                }
            }.onFailure { error ->
                chatState = chatState.copy(error = error.message ?: "audio playback failed")
            }
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
            noticeJob?.cancel()
            chatState = ChatState()
            noticeState = NoticeState()
            profileState = ProfileState()
            followState = FollowState()
            safetyState = SafetyState()
        }
    }

    override fun onDestroy() {
        mediaRecorder?.let { recorder ->
            runCatching { recorder.stop() }
            recorder.release()
        }
        mediaRecorder = null
        recordingFile?.delete()
        audioPlayer?.release()
        audioPlayer = null
        client.close()
        scope.cancel()
        super.onDestroy()
    }
}