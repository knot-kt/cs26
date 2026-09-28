package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
enum class ConversationType {
    DIRECT,
    GROUP,
}

@Serializable
enum class GroupMemberRole {
    OWNER,
    MEMBER,
}

@Serializable
data class GroupMember(
    val userId: String,
    val role: GroupMemberRole,
)

@Serializable
data class GroupConversation(
    val id: String,
    val type: ConversationType = ConversationType.GROUP,
    val name: String,
    val ownerId: String,
    val members: List<GroupMember> = emptyList(),
)

@Serializable
data class ConversationPage(
    val items: List<GroupConversation>,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
)

@Serializable
data class GroupInviteRequest(
    val userId: String,
)

@Serializable
data class GroupInviteReceipt(
    val conversationId: String,
    val invitedUserId: String,
)