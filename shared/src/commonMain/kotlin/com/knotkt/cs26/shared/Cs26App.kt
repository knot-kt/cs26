package com.knotkt.cs26.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Cs26App(
    healthState: HealthState,
    onCheckHealth: () -> Unit,
    authState: AuthState,
    onPhoneChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onRequestCode: () -> Unit,
    onVerifyCode: () -> Unit,
    onLogout: () -> Unit,
    postState: PostState,
    onPostContentChanged: (String) -> Unit,
    onPublishPost: () -> Unit,
    onRefreshPosts: () -> Unit,
    onAddImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleLike: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onLoadComments: (String) -> Unit,
    onCommentInputChanged: (String, String) -> Unit,
    onAddComment: (String) -> Unit,
    chatState: ChatState,
    onChatInputChanged: (String) -> Unit,
    onConnectChat: () -> Unit,
    onSendChat: () -> Unit,
    onAddChatImage: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    noticeState: NoticeState,
    onRefreshNotices: () -> Unit,
    onMarkNoticeRead: (String) -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("CS26", style = MaterialTheme.typography.headlineMedium)
                Text("Local development sign-in", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = authState.phone,
                    onValueChange = onPhoneChanged,
                    label = { Text("Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = onRequestCode,
                    enabled = authState.phone.isNotBlank() && !authState.isRequestingCode,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (authState.isRequestingCode) "Requesting" else "Request code")
                }
                if (authState.developmentCode != null) {
                    Text("Development code: ${authState.developmentCode}")
                }
                OutlinedTextField(
                    value = authState.code,
                    onValueChange = onCodeChanged,
                    label = { Text("Verification code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = onVerifyCode,
                    enabled = authState.code.isNotBlank() && !authState.isVerifyingCode,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (authState.isVerifyingCode) "Signing in" else "Sign in")
                }
                if (authState.session != null) {
                    Text("Signed in as ${authState.session.userId}")
                    var selectedTab by rememberSaveable { mutableStateOf(0) }
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf("Activity", "Chat", "Notices", "Me").forEachIndexed { index, label ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(label) },
                            )
                        }
                    }
                    when (selectedTab) {
                        0 -> {
                    Text("Activity", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = postState.content,
                        onValueChange = onPostContentChanged,
                        label = { Text("Share an update") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                    )
                    Button(
                        onClick = onPublishPost,
                        enabled = (postState.content.isNotBlank() || postState.attachments.isNotEmpty()) &&
                            !postState.isPublishing && !postState.isUploading && !postState.isInteracting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (postState.isPublishing) "Publishing" else "Publish")
                    }
                    Button(
                        onClick = onAddImage,
                        enabled = postState.attachments.size < 9 && !postState.isUploading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (postState.isUploading) "Uploading" else "Add image")
                    }
                    Button(
                        onClick = onTakePhoto,
                        enabled = postState.attachments.size < 9 && !postState.isUploading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Take photo")
                    }
                    postState.attachments.forEach { attachment ->
                        Text("Attached ${attachment.objectKey} (${attachment.sizeBytes} bytes)")
                    }
                    Button(
                        onClick = onRefreshPosts,
                        enabled = !postState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (postState.isLoading) "Refreshing" else "Refresh activity")
                    }
                    postState.posts.forEach { post ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(post.content)
                            Text(
                                text = "${post.authorId} · ${post.createdAtEpochMillis}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text("Likes: ${post.likeCount} · Comments: ${post.commentCount}")
                            Button(
                                onClick = { onToggleLike(post.id) },
                                enabled = !postState.isInteracting,
                            ) {
                                Text(if (post.likedByViewer) "Unlike" else "Like")
                            }
                            if (post.authorId == authState.session.userId) {
                                Button(
                                    onClick = { onDeletePost(post.id) },
                                    enabled = !postState.isInteracting,
                                ) {
                                    Text("Delete")
                                }
                            }
                            Button(
                                onClick = { onLoadComments(post.id) },
                                enabled = !postState.isInteracting,
                            ) {
                                Text("Load comments")
                            }
                            postState.commentsByPost[post.id].orEmpty().forEach { comment ->
                                Text("${comment.authorId}: ${comment.content}")
                            }
                            OutlinedTextField(
                                value = postState.commentInputs[post.id].orEmpty(),
                                onValueChange = { value -> onCommentInputChanged(post.id, value) },
                                label = { Text("Comment") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = { onAddComment(post.id) },
                                enabled = postState.commentInputs[post.id].orEmpty().isNotBlank() &&
                                    !postState.isInteracting,
                            ) {
                                Text("Add comment")
                            }
                        }
                    }
                    if (postState.error != null) {
                        Text(
                            text = "Post error: ${postState.error}",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                        }
                        1 -> {
                    Text("Chat", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = onConnectChat,
                        enabled = !chatState.isConnecting && !chatState.isConnected,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            when {
                                chatState.isConnected -> "Connected"
                                chatState.isConnecting && chatState.error != null -> "Reconnecting"
                                chatState.isConnecting -> "Connecting"
                                else -> "Connect chat"
                            },
                        )
                    }
                    if (chatState.pendingMessages.isNotEmpty()) {
                        Text("Pending messages: ${chatState.pendingMessages.size}")
                    }
                    if (chatState.lastDeliveryStatus != null) {
                        Text("Last delivery: ${chatState.lastDeliveryStatus}")
                    }
                    chatState.messages.forEach { message ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("${message.senderId}: ${message.content}")
                            message.attachments.forEach { attachment ->
                                Text("${attachment.kind}: ${attachment.objectKey} (${attachment.sizeBytes} bytes)")
                                if (attachment.kind == com.knotkt.cs26.contracts.MediaKind.AUDIO) {
                                    Button(onClick = { onPlayAudio(attachment.objectKey) }) {
                                        Text("Play audio")
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = chatState.input,
                        onValueChange = onChatInputChanged,
                        label = { Text("Message") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = onSendChat,
                        enabled = (chatState.input.isNotBlank() || chatState.attachments.isNotEmpty()) &&
                            !chatState.isUploading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Send")
                    }
                    Button(
                        onClick = onAddChatImage,
                        enabled = chatState.attachments.size < 9 && !chatState.isUploading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (chatState.isUploading) "Uploading" else "Add image")
                    }
                    Button(
                        onClick = if (chatState.isRecording) onStopRecording else onStartRecording,
                        enabled = !chatState.isUploading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (chatState.isRecording) "Stop recording" else "Record audio")
                    }
                    chatState.attachments.forEach { attachment ->
                        Text("Pending ${attachment.kind}: ${attachment.objectKey} (${attachment.sizeBytes} bytes)")
                    }
                    if (chatState.error != null) {
                        Text("Chat error: ${chatState.error}", color = MaterialTheme.colorScheme.error)
                    }
                        }
                        2 -> {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    Text(if (noticeState.isConnecting) "Live updates connected" else "Live updates reconnecting")
                    Button(
                        onClick = onRefreshNotices,
                        enabled = !noticeState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (noticeState.isLoading) "Refreshing" else "Refresh notifications")
                    }
                    noticeState.notices.forEach { notice ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Text(if (notice.read) notice.title else "[Unread] ${notice.title}")
                            Text(notice.body)
                            if (notice.deepLink != null) Text("Open: ${notice.deepLink}")
                            if (!notice.read) {
                                Button(onClick = { onMarkNoticeRead(notice.id) }) {
                                    Text("Mark read")
                                }
                            }
                        }
                    }
                    if (noticeState.error != null) {
                        Text("Notification error: ${noticeState.error}", color = MaterialTheme.colorScheme.error)
                    }
                        }
                        3 -> {
                            Text("Profile", style = MaterialTheme.typography.titleMedium)
                            Text("User ID: ${authState.session.userId}")
                            Text("Session and account settings")
                            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                                Text("Sign out")
                            }
                        }
                    }
                }
                if (authState.error != null) {
                    Text(
                        text = "Auth error: ${authState.error}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text("Health vertical slice", modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = when {
                        healthState.isLoading -> "Checking server..."
                        healthState.response != null -> "Server: ${healthState.response.status}"
                        healthState.error != null -> "Server unavailable: ${healthState.error}"
                        else -> "Server status not checked"
                    },
                    modifier = Modifier.padding(top = 16.dp),
                )
                Button(
                    onClick = onCheckHealth,
                    enabled = !healthState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (healthState.isLoading) "Checking" else "Check server")
                }
            }
        }
    }
}