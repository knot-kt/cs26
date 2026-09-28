package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.GroupConversation
import com.knotkt.cs26.contracts.GroupInviteReceipt
import com.knotkt.cs26.contracts.GroupMember
import com.knotkt.cs26.contracts.GroupMemberRole
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface GroupStore {
    fun create(ownerId: String, name: String): GroupConversation

    fun get(conversationId: String): GroupConversation?

    fun listFor(userId: String): List<GroupConversation>

    fun invite(ownerId: String, conversationId: String, userId: String): GroupInviteReceipt?

    fun accept(userId: String, conversationId: String): GroupConversation?

    fun leave(userId: String, conversationId: String): Boolean

    fun isGroup(conversationId: String): Boolean

    fun isMember(userId: String, conversationId: String): Boolean
}

class InMemoryGroupStore(
    private val maxMembers: Int = 20,
) : GroupStore {
    private data class GroupRecord(
        val id: String,
        val name: String,
        val ownerId: String,
        val members: MutableMap<String, GroupMemberRole>,
        val pendingInvites: MutableSet<String>,
    )

    private val groups = ConcurrentHashMap<String, GroupRecord>()

    override fun create(ownerId: String, name: String): GroupConversation {
        val record = GroupRecord(
            id = UUID.randomUUID().toString(),
            name = name,
            ownerId = ownerId,
            members = ConcurrentHashMap<String, GroupMemberRole>().also { it[ownerId] = GroupMemberRole.OWNER },
            pendingInvites = ConcurrentHashMap.newKeySet(),
        )
        groups[record.id] = record
        return record.toContract()
    }

    override fun get(conversationId: String): GroupConversation? = groups[conversationId]?.toContract()

    override fun listFor(userId: String): List<GroupConversation> = groups.values
        .filter { userId in it.members }
        .sortedBy { it.id }
        .map { it.toContract() }

    @Synchronized
    override fun invite(ownerId: String, conversationId: String, userId: String): GroupInviteReceipt? {
        val group = groups[conversationId] ?: return null
        if (group.ownerId != ownerId || userId.isBlank() || userId in group.members || userId in group.pendingInvites) return null
        if (group.members.size + group.pendingInvites.size >= maxMembers) return null
        group.pendingInvites.add(userId)
        return GroupInviteReceipt(conversationId, userId)
    }

    @Synchronized
    override fun accept(userId: String, conversationId: String): GroupConversation? {
        val group = groups[conversationId] ?: return null
        if (!group.pendingInvites.remove(userId) || group.members.size >= maxMembers) return null
        group.members[userId] = GroupMemberRole.MEMBER
        return group.toContract()
    }

    @Synchronized
    override fun leave(userId: String, conversationId: String): Boolean {
        val group = groups[conversationId] ?: return false
        if (group.ownerId == userId || userId !in group.members) return false
        group.members.remove(userId)
        return true
    }

    override fun isGroup(conversationId: String): Boolean = groups.containsKey(conversationId)

    override fun isMember(userId: String, conversationId: String): Boolean =
        groups[conversationId]?.members?.containsKey(userId) == true

    private fun GroupRecord.toContract(): GroupConversation = GroupConversation(
        id = id,
        name = name,
        ownerId = ownerId,
        members = members.entries
            .sortedBy { it.key }
            .map { (userId, role) -> GroupMember(userId, role) },
    )
}