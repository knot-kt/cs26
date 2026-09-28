@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.knotkt.cs26.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knotkt.cs26.contracts.MediaKind
import com.knotkt.cs26.contracts.PostType

private val Cs26Navy = Color(0xFF14345F)
private val Cs26Yellow = Color(0xFFFFC857)
private val Cs26Background = Color(0xFFF7F8FA)

private enum class SocialDestination(val label: String) {
    PLAZA("广场"),
    MESSAGES("消息"),
    REMINDERS("提醒"),
    ME("我"),
}

@Suppress("UNUSED_PARAMETER")
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
    onPostTypeChanged: (PostType) -> Unit,
    onPublishPost: () -> Unit,
    onRefreshPosts: () -> Unit,
    onLoadMorePosts: () -> Unit,
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
    profileState: ProfileState,
    onProfileNicknameChanged: (String) -> Unit,
    onProfileInterestsChanged: (String) -> Unit,
    onProfileAnonymousChanged: (Boolean) -> Unit,
    onRefreshProfile: () -> Unit,
    onSaveProfile: () -> Unit,
    followState: FollowState,
    onToggleFollow: (String) -> Unit,
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Cs26Navy,
            secondary = Cs26Yellow,
            background = Cs26Background,
            surface = Color.White,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Cs26Background) {
            if (authState.session == null) {
                AuthScreen(
                    authState = authState,
                    onPhoneChanged = onPhoneChanged,
                    onCodeChanged = onCodeChanged,
                    onRequestCode = onRequestCode,
                    onVerifyCode = onVerifyCode,
                )
            } else {
                SocialShell(
                    viewerId = authState.session.userId,
                    onLogout = onLogout,
                    postState = postState,
                    onPostContentChanged = onPostContentChanged,
                    onPostTypeChanged = onPostTypeChanged,
                    onPublishPost = onPublishPost,
                    onRefreshPosts = onRefreshPosts,
                    onLoadMorePosts = onLoadMorePosts,
                    onAddImage = onAddImage,
                    onTakePhoto = onTakePhoto,
                    onToggleLike = onToggleLike,
                    onDeletePost = onDeletePost,
                    onLoadComments = onLoadComments,
                    onCommentInputChanged = onCommentInputChanged,
                    onAddComment = onAddComment,
                    chatState = chatState,
                    onChatInputChanged = onChatInputChanged,
                    onConnectChat = onConnectChat,
                    onSendChat = onSendChat,
                    onAddChatImage = onAddChatImage,
                    onPlayAudio = onPlayAudio,
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording,
                    noticeState = noticeState,
                    onRefreshNotices = onRefreshNotices,
                    onMarkNoticeRead = onMarkNoticeRead,
                    profileState = profileState,
                    onProfileNicknameChanged = onProfileNicknameChanged,
                    onProfileInterestsChanged = onProfileInterestsChanged,
                    onProfileAnonymousChanged = onProfileAnonymousChanged,
                    onRefreshProfile = onRefreshProfile,
                    onSaveProfile = onSaveProfile,
                    followState = followState,
                    onToggleFollow = onToggleFollow,
                )
            }
        }
    }
}

@Composable
private fun AuthScreen(
    authState: AuthState,
    onPhoneChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onRequestCode: () -> Unit,
    onVerifyCode: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("CS26", style = MaterialTheme.typography.displaySmall, color = Cs26Navy)
        Text("和同学分享想法，发现感兴趣的人", style = MaterialTheme.typography.titleMedium)
        Text("使用手机号进入社区，公开资料只使用昵称和头像。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = authState.phone,
            onValueChange = onPhoneChanged,
            label = { Text("手机号") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onRequestCode,
            enabled = authState.phone.isNotBlank() && !authState.isRequestingCode,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (authState.isRequestingCode) "正在发送" else "获取验证码")
        }
        if (authState.developmentCode != null) {
            Text(
                "本地开发验证码：${authState.developmentCode}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        OutlinedTextField(
            value = authState.code,
            onValueChange = onCodeChanged,
            label = { Text("验证码") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onVerifyCode,
            enabled = authState.code.isNotBlank() && !authState.isVerifyingCode,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (authState.isVerifyingCode) "正在进入" else "进入 CS26")
        }
        if (authState.error != null) {
            Text(authState.error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SocialShell(
    viewerId: String,
    onLogout: () -> Unit,
    postState: PostState,
    onPostContentChanged: (String) -> Unit,
    onPostTypeChanged: (PostType) -> Unit,
    onPublishPost: () -> Unit,
    onRefreshPosts: () -> Unit,
    onLoadMorePosts: () -> Unit,
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
    profileState: ProfileState,
    onProfileNicknameChanged: (String) -> Unit,
    onProfileInterestsChanged: (String) -> Unit,
    onProfileAnonymousChanged: (Boolean) -> Unit,
    onRefreshProfile: () -> Unit,
    onSaveProfile: () -> Unit,
    followState: FollowState,
    onToggleFollow: (String) -> Unit,
) {
    var selectedDestination by rememberSaveable { mutableStateOf(SocialDestination.PLAZA) }
    var showComposer by rememberSaveable { mutableStateOf(false) }
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CS26", fontWeight = FontWeight.Bold)
                        Text(
                            selectedDestination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                SocialDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { selectedDestination = destination },
                        icon = { Text(destination.label.take(1)) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedDestination == SocialDestination.PLAZA) {
                FloatingActionButton(
                    onClick = { showComposer = true },
                    containerColor = Cs26Yellow,
                    contentColor = Cs26Navy,
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        containerColor = Cs26Background,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (selectedDestination) {
                SocialDestination.PLAZA -> PlazaScreen(
                    viewerId = viewerId,
                    postState = postState,
                    onOpenComposer = { showComposer = true },
                    onOpenPost = { postId ->
                        selectedPostId = postId
                        onLoadComments(postId)
                    },
                    onRefreshPosts = onRefreshPosts,
                    onLoadMorePosts = onLoadMorePosts,
                    onToggleLike = onToggleLike,
                    onDeletePost = onDeletePost,
                    onLoadComments = onLoadComments,
                    onCommentInputChanged = onCommentInputChanged,
                    onAddComment = onAddComment,
                    followState = followState,
                    onToggleFollow = onToggleFollow,
                )

                SocialDestination.MESSAGES -> MessagesScreen(
                    viewerId = viewerId,
                    chatState = chatState,
                    onChatInputChanged = onChatInputChanged,
                    onConnectChat = onConnectChat,
                    onSendChat = onSendChat,
                    onAddChatImage = onAddChatImage,
                    onPlayAudio = onPlayAudio,
                    onStartRecording = onStartRecording,
                    onStopRecording = onStopRecording,
                )

                SocialDestination.REMINDERS -> RemindersScreen(
                    noticeState = noticeState,
                    onRefreshNotices = onRefreshNotices,
                    onMarkNoticeRead = onMarkNoticeRead,
                )

                SocialDestination.ME -> ProfileScreen(
                    profileState = profileState,
                    onProfileNicknameChanged = onProfileNicknameChanged,
                    onProfileInterestsChanged = onProfileInterestsChanged,
                    onProfileAnonymousChanged = onProfileAnonymousChanged,
                    onRefreshProfile = onRefreshProfile,
                    onSaveProfile = onSaveProfile,
                    onLogout = onLogout,
                )
            }
        }
    }

    if (showComposer) {
        PostComposerSheet(
            postState = postState,
            onDismiss = { showComposer = false },
            onPostContentChanged = onPostContentChanged,
            onPostTypeChanged = onPostTypeChanged,
            onPublishPost = onPublishPost,
            onAddImage = onAddImage,
            onTakePhoto = onTakePhoto,
        )
    }

    val selectedPost = postState.posts.firstOrNull { it.id == selectedPostId }
    if (selectedPost != null) {
        PostDetailSheet(
            postState = postState,
            post = selectedPost,
            viewerId = viewerId,
            onDismiss = { selectedPostId = null },
            onToggleLike = onToggleLike,
            onDeletePost = onDeletePost,
            onCommentInputChanged = onCommentInputChanged,
            onAddComment = onAddComment,
        )
    }
}

@Composable
private fun PlazaScreen(
    viewerId: String,
    postState: PostState,
    onOpenComposer: () -> Unit,
    onOpenPost: (String) -> Unit,
    onRefreshPosts: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onToggleLike: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onLoadComments: (String) -> Unit,
    onCommentInputChanged: (String, String) -> Unit,
    onAddComment: (String) -> Unit,
    followState: FollowState,
    onToggleFollow: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("今天想分享什么？", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onOpenComposer, modifier = Modifier.fillMaxWidth()) {
                    Text("发布一条动态")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TopicChip("推荐")
                    TopicChip("学习")
                    TopicChip("闲置")
                    TopicChip("求助")
                }
                OutlinedButton(
                    onClick = onRefreshPosts,
                    enabled = !postState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (postState.isLoading) "正在刷新" else "刷新广场")
                }
            }
        }
        if (postState.posts.isEmpty() && !postState.isLoading) {
            item {
                EmptyState(
                    title = "广场还没有动态",
                    message = "发布第一条内容，和同学开始交流。",
                    action = "去发布",
                    onAction = onOpenComposer,
                )
            }
        }
        items(postState.posts, key = { it.id }) { post ->
            PostCard(
                viewerId = viewerId,
                postState = postState,
                post = post,
                onOpenPost = onOpenPost,
                onToggleLike = onToggleLike,
                onDeletePost = onDeletePost,
                onLoadComments = onLoadComments,
                onCommentInputChanged = onCommentInputChanged,
                onAddComment = onAddComment,
                followState = followState,
                onToggleFollow = onToggleFollow,
            )
        }
        if (postState.error != null) {
            item {
                Text("动态处理失败：${postState.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        if (postState.nextCursor != null) {
            item {
                OutlinedButton(
                    onClick = onLoadMorePosts,
                    enabled = !postState.isLoadingMore,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (postState.isLoadingMore) "正在加载" else "加载更多")
                }
            }
        }
    }
}

@Composable
private fun TopicChip(label: String) {
    Surface(
        color = Color.White,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp,
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    action: String,
    onAction: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun PostComposerSheet(
    postState: PostState,
    onDismiss: () -> Unit,
    onPostContentChanged: (String) -> Unit,
    onPostTypeChanged: (PostType) -> Unit,
    onPublishPost: () -> Unit,
    onAddImage: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("发布动态", style = MaterialTheme.typography.titleLarge)
            Text("选择内容类型", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostTypeOption(PostType.GENERAL, postState.type, onPostTypeChanged)
                PostTypeOption(PostType.RANT, postState.type, onPostTypeChanged)
                PostTypeOption(PostType.HELP, postState.type, onPostTypeChanged)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostTypeOption(PostType.EVENT, postState.type, onPostTypeChanged)
                PostTypeOption(PostType.LOST_AND_FOUND, postState.type, onPostTypeChanged)
                PostTypeOption(PostType.USED_ITEM, postState.type, onPostTypeChanged)
            }
            OutlinedTextField(
                value = postState.content,
                onValueChange = onPostContentChanged,
                label = { Text("写下你的想法") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAddImage,
                    enabled = postState.attachments.size < 9 && !postState.isUploading,
                ) {
                    Text(if (postState.isUploading) "上传中" else "添加图片")
                }
                OutlinedButton(
                    onClick = onTakePhoto,
                    enabled = postState.attachments.size < 9 && !postState.isUploading,
                ) {
                    Text("拍照")
                }
            }
            postState.attachments.forEach { attachment ->
                Text(
                    "已添加图片：${attachment.objectKey}（${attachment.sizeBytes} bytes）",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (postState.error != null) {
                Text("发布失败：${postState.error}", color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onPublishPost,
                enabled = (postState.content.isNotBlank() || postState.attachments.isNotEmpty()) &&
                    !postState.isPublishing && !postState.isUploading && !postState.isInteracting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (postState.isPublishing) "发布中" else "发布")
            }
        }
    }
}

@Composable
private fun PostTypeOption(
    type: PostType,
    selectedType: PostType,
    onSelected: (PostType) -> Unit,
) {
    if (type == selectedType) {
        Button(onClick = { onSelected(type) }) {
            Text(type.label())
        }
    } else {
        OutlinedButton(onClick = { onSelected(type) }) {
            Text(type.label())
        }
    }
}

private fun PostType.label(): String = when (this) {
    PostType.GENERAL -> "普通"
    PostType.RANT -> "吐槽"
    PostType.HELP -> "求助"
    PostType.EVENT -> "活动"
    PostType.LOST_AND_FOUND -> "失物"
    PostType.INTEREST -> "兴趣"
    PostType.USED_ITEM -> "闲置"
}

@Composable
private fun PostCard(
    viewerId: String,
    postState: PostState,
    post: com.knotkt.cs26.contracts.Post,
    onOpenPost: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onLoadComments: (String) -> Unit,
    onCommentInputChanged: (String, String) -> Unit,
    onAddComment: (String) -> Unit,
    followState: FollowState,
    onToggleFollow: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = Cs26Yellow,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("同") }
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text("成员", fontWeight = FontWeight.SemiBold)
                    Text(
                        "刚刚发布",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (post.authorId != viewerId) {
                    OutlinedButton(
                        onClick = { onToggleFollow(post.authorId) },
                        enabled = !followState.isLoading,
                    ) {
                        Text(if (post.authorId in followState.followingIds) "已关注" else "关注")
                    }
                }
            }
            if (post.content.isNotBlank()) Text(post.content)
            Text(
                post.type.label(),
                color = Cs26Navy,
                style = MaterialTheme.typography.labelMedium,
            )
            if (post.attachments.isNotEmpty()) {
                Text("图片 ${post.attachments.size} 张", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = { onOpenPost(post.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("查看详情")
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(
                    onClick = { onToggleLike(post.id) },
                    enabled = !postState.isInteracting,
                ) {
                    Text(if (post.likedByViewer) "已喜欢 ${post.likeCount}" else "喜欢 ${post.likeCount}")
                }
                OutlinedButton(
                    onClick = { onLoadComments(post.id) },
                    enabled = !postState.isInteracting,
                ) {
                    Text("评论 ${post.commentCount}")
                }
                if (post.authorId == viewerId) {
                    OutlinedButton(
                        onClick = { onDeletePost(post.id) },
                        enabled = !postState.isInteracting,
                    ) {
                        Text("删除")
                    }
                }
            }
            postState.commentsByPost[post.id].orEmpty().forEach { comment ->
                Text("成员：${comment.content}", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(
                value = postState.commentInputs[post.id].orEmpty(),
                onValueChange = { value -> onCommentInputChanged(post.id, value) },
                label = { Text("写评论") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onAddComment(post.id) },
                enabled = postState.commentInputs[post.id].orEmpty().isNotBlank() &&
                    !postState.isInteracting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("发送评论")
            }
        }
    }
}

@Composable
private fun PostDetailSheet(
    postState: PostState,
    post: com.knotkt.cs26.contracts.Post,
    viewerId: String,
    onDismiss: () -> Unit,
    onToggleLike: (String) -> Unit,
    onDeletePost: (String) -> Unit,
    onCommentInputChanged: (String, String) -> Unit,
    onAddComment: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Cs26Yellow,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("同") }
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text("成员", fontWeight = FontWeight.SemiBold)
                    Text("动态详情", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(post.type.label(), color = Cs26Navy, style = MaterialTheme.typography.labelMedium)
            if (post.content.isNotBlank()) {
                Text(post.content, style = MaterialTheme.typography.bodyLarge)
            }
            if (post.attachments.isNotEmpty()) {
                Text("图片 ${post.attachments.size} 张", color = MaterialTheme.colorScheme.onSurfaceVariant)
                post.attachments.forEach { attachment ->
                    Text(attachment.objectKey, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onToggleLike(post.id) }, enabled = !postState.isInteracting) {
                    Text(if (post.likedByViewer) "已喜欢 ${post.likeCount}" else "喜欢 ${post.likeCount}")
                }
                if (post.authorId == viewerId) {
                    OutlinedButton(onClick = { onDeletePost(post.id) }, enabled = !postState.isInteracting) {
                        Text("删除")
                    }
                }
            }
            HorizontalDivider()
            Text("评论 ${post.commentCount}", style = MaterialTheme.typography.titleMedium)
            postState.commentsByPost[post.id].orEmpty().forEach { comment ->
                Text("成员：${comment.content}")
            }
            OutlinedTextField(
                value = postState.commentInputs[post.id].orEmpty(),
                onValueChange = { value -> onCommentInputChanged(post.id, value) },
                label = { Text("写评论") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onAddComment(post.id) },
                enabled = postState.commentInputs[post.id].orEmpty().isNotBlank() &&
                    !postState.isInteracting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("发送评论")
            }
        }
    }
}

@Composable
private fun MessagesScreen(
    viewerId: String,
    chatState: ChatState,
    onChatInputChanged: (String) -> Unit,
    onConnectChat: () -> Unit,
    onSendChat: () -> Unit,
    onAddChatImage: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("演示会话", style = MaterialTheme.typography.titleMedium)
                Text(
                    "会话列表和群聊将在消息数据模型接入后显示。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onConnectChat,
                    enabled = !chatState.isConnecting && !chatState.isConnected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            chatState.isConnected -> "已连接"
                            chatState.isConnecting && chatState.error != null -> "正在重连"
                            chatState.isConnecting -> "正在连接"
                            else -> "连接消息服务"
                        },
                    )
                }
            }
        }
        if (chatState.pendingMessages.isNotEmpty()) {
            Text("待发送 ${chatState.pendingMessages.size} 条", color = Cs26Navy)
        }
        if (chatState.lastDeliveryStatus != null) {
            Text("最近状态：${chatState.lastDeliveryStatus}")
        }
        chatState.messages.forEach { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (message.senderId == viewerId) Color(0xFFE8F0FC) else Color.White,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(if (message.senderId == viewerId) "我" else "对方", fontWeight = FontWeight.SemiBold)
                    if (message.content.isNotBlank()) Text(message.content)
                    message.attachments.forEach { attachment ->
                        Text("${attachment.kind} 附件：${attachment.objectKey}")
                        if (attachment.kind == MediaKind.AUDIO) {
                            OutlinedButton(onClick = { onPlayAudio(attachment.objectKey) }) {
                                Text("播放语音")
                            }
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            value = chatState.input,
            onValueChange = onChatInputChanged,
            label = { Text("输入消息") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onSendChat,
            enabled = (chatState.input.isNotBlank() || chatState.attachments.isNotEmpty()) &&
                !chatState.isUploading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("发送")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAddChatImage,
                enabled = chatState.attachments.size < 9 && !chatState.isUploading,
            ) {
                Text(if (chatState.isUploading) "上传中" else "添加图片")
            }
            OutlinedButton(
                onClick = if (chatState.isRecording) onStopRecording else onStartRecording,
                enabled = !chatState.isUploading,
            ) {
                Text(if (chatState.isRecording) "停止录音" else "录音")
            }
        }
        chatState.attachments.forEach { attachment ->
            Text("待发送 ${attachment.kind}：${attachment.objectKey}", style = MaterialTheme.typography.bodySmall)
        }
        if (chatState.error != null) {
            Text("消息处理失败：${chatState.error}", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun RemindersScreen(
    noticeState: NoticeState,
    onRefreshNotices: () -> Unit,
    onMarkNoticeRead: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Button(
                onClick = onRefreshNotices,
                enabled = !noticeState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (noticeState.isLoading) "正在刷新" else "刷新提醒")
            }
        }
        if (noticeState.notices.isEmpty() && !noticeState.isLoading) {
            item {
                EmptyState(
                    title = "还没有提醒",
                    message = "新的互动和系统消息会出现在这里。",
                    action = "刷新",
                    onAction = onRefreshNotices,
                )
            }
        }
        items(noticeState.notices, key = { it.id }) { notice ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (notice.read) notice.title else "未读 · ${notice.title}", fontWeight = FontWeight.SemiBold)
                    Text(notice.body)
                    if (!notice.read) {
                        OutlinedButton(onClick = { onMarkNoticeRead(notice.id) }) {
                            Text("标记已读")
                        }
                    }
                }
            }
        }
        if (noticeState.error != null) {
            item {
                Text("提醒处理失败：${noticeState.error}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    profileState: ProfileState,
    onProfileNicknameChanged: (String) -> Unit,
    onProfileInterestsChanged: (String) -> Unit,
    onProfileAnonymousChanged: (Boolean) -> Unit,
    onRefreshProfile: () -> Unit,
    onSaveProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    color = Cs26Yellow,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("我", style = MaterialTheme.typography.titleLarge) }
                }
                Text("我的空间", style = MaterialTheme.typography.titleLarge)
                Text(
                    "只使用昵称和兴趣认识彼此，手机号不会展示。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = profileState.nickname,
                    onValueChange = onProfileNicknameChanged,
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = profileState.interestsInput,
                    onValueChange = onProfileInterestsChanged,
                    label = { Text("兴趣标签") },
                    supportingText = { Text("用逗号分隔，最多 10 个") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = profileState.anonymousByDefault,
                        onCheckedChange = { checked -> onProfileAnonymousChanged(checked) },
                    )
                    Text("默认以匿名身份发布")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onSaveProfile,
                        enabled = !profileState.isSaving && !profileState.isLoading,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (profileState.isSaving) "保存中" else "保存资料")
                    }
                    OutlinedButton(
                        onClick = onRefreshProfile,
                        enabled = !profileState.isLoading && !profileState.isSaving,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (profileState.isLoading) "读取中" else "刷新")
                    }
                }
                if (profileState.error != null) {
                    Text(profileState.error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("退出登录")
        }
    }
}