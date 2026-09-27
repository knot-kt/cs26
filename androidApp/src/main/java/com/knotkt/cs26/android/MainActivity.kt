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
import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.MediaKind
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    private val healthRepository = HealthRepository(client, "http://10.0.2.2:8080")
    private val authRepository = AuthRepository(client, "http://10.0.2.2:8080")
    private val postRepository = PostRepository(client, "http://10.0.2.2:8080")
    private var healthState by mutableStateOf(HealthState())
    private var authState by mutableStateOf(AuthState())
    private var postState by mutableStateOf(PostState())
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
        }
    }

    override fun onDestroy() {
        client.close()
        scope.cancel()
        super.onDestroy()
    }
}