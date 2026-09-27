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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                            !postState.isPublishing && !postState.isUploading,
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
                        }
                    }
                    if (postState.error != null) {
                        Text(
                            text = "Post error: ${postState.error}",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                        Text("Sign out")
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