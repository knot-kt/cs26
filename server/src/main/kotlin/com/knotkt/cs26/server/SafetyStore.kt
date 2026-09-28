package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.CreateReportRequest
import com.knotkt.cs26.contracts.ReportReceipt
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface SafetyStore {
    fun createReport(reporterId: String, request: CreateReportRequest): ReportReceipt

    fun setBlocked(blockerId: String, blockedId: String, blocked: Boolean): Boolean

    fun isBlocked(blockerId: String, blockedId: String): Boolean

    fun listBlocked(blockerId: String): List<String>
}

class InMemorySafetyStore : SafetyStore {
    private val blockedUsers = ConcurrentHashMap<String, MutableSet<String>>()

    override fun createReport(reporterId: String, request: CreateReportRequest): ReportReceipt = ReportReceipt(
        reportId = UUID.randomUUID().toString(),
        targetType = request.targetType,
        targetId = request.targetId,
    )

    override fun setBlocked(blockerId: String, blockedId: String, blocked: Boolean): Boolean {
        if (blockerId == blockedId) return false
        val blockedUsers = blockedUsers.computeIfAbsent(blockerId) { ConcurrentHashMap.newKeySet() }
        if (blocked) blockedUsers.add(blockedId) else blockedUsers.remove(blockedId)
        return true
    }

    override fun isBlocked(blockerId: String, blockedId: String): Boolean =
        blockedUsers[blockerId]?.contains(blockedId) == true

    override fun listBlocked(blockerId: String): List<String> =
        blockedUsers[blockerId].orEmpty().toList().sorted()
}