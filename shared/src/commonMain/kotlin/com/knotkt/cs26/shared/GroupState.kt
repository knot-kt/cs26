package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.GroupConversation

data class GroupState(
    val groups: List<GroupConversation> = emptyList(),
    val nameInput: String = "",
    val selectedConversationId: String? = null,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
)