package com.example.reviews

import java.util.UUID

enum class DisputeStatus(val label: String) {
    OPEN("Under Review"),
    MEDIATING("SB Moderation"),
    RESOLVED("Resolved & Closed"),
    REJECTED("Rejected")
}

data class MadeItReview(
    val id: String = "REV-${UUID.randomUUID().toString().substring(0, 6).uppercase()}",
    val storeId: String,
    val storeName: String,
    val customerName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val createdAt: String = "2 days ago",
    val providerReply: String? = null,
    val isVerifiedPurchase: Boolean = true
)

data class MadeItDispute(
    val id: String = "DSP-${UUID.randomUUID().toString().substring(0, 6).uppercase()}",
    val orderId: String,
    val storeName: String,
    val reason: String,
    val description: String,
    val status: DisputeStatus = DisputeStatus.OPEN,
    val requestedRefundAmount: Double? = null,
    val adminResolutionNotes: String? = null,
    val createdAt: String = "Yesterday"
)
