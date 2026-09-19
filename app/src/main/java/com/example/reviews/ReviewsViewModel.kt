package com.example.reviews

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReviewsViewModel(application: Application) : AndroidViewModel(application) {
    private val _reviews = MutableStateFlow<List<MadeItReview>>(
        listOf(
            MadeItReview(
                id = "REV-A109",
                storeId = "store_kumar_solutions",
                storeName = "Kumar Home & Hardware Hub",
                customerName = "Rohan Sharma",
                rating = 5,
                comment = "Arrived on time within 30 minutes. Fixed bathroom water tap leaking issue very cleanly. Highly recommended!",
                createdAt = "Yesterday",
                providerReply = "Thank you Rohan! Happy to assist you anytime with your home plumbing.",
                isVerifiedPurchase = true
            ),
            MadeItReview(
                id = "REV-B402",
                storeId = "store_sri_lakshmi",
                storeName = "Sri Lakshmi Organic Provisions",
                customerName = "Pooja Hegde",
                rating = 4,
                comment = "Very fresh vegetables and quick doorstep delivery in 20 minutes.",
                createdAt = "3 days ago",
                providerReply = null,
                isVerifiedPurchase = true
            ),
            MadeItReview(
                id = "REV-C881",
                storeId = "store_kumar_solutions",
                storeName = "Kumar Home & Hardware Hub",
                customerName = "Venkatesh Rao",
                rating = 5,
                comment = "Genuine CPVC pipes and plumbing parts at wholesale rates.",
                createdAt = "Last week",
                providerReply = "Glad to serve you sir!",
                isVerifiedPurchase = true
            )
        )
    )
    val reviews: StateFlow<List<MadeItReview>> = _reviews.asStateFlow()

    private val _disputes = MutableStateFlow<List<MadeItDispute>>(
        listOf(
            MadeItDispute(
                id = "DSP-1029",
                orderId = "ORD-44BC719D",
                storeName = "Sri Lakshmi Organic Provisions",
                reason = "Damaged package received",
                description = "Milk packet was torn upon delivery. Requested refund for item.",
                status = DisputeStatus.MEDIATING,
                requestedRefundAmount = 64.0,
                adminResolutionNotes = "SB Creatives support reached out to shop owner for replacement dispatch.",
                createdAt = "Yesterday"
            ),
            MadeItDispute(
                id = "DSP-0812",
                orderId = "ORD-12FE56A2",
                storeName = "Kumar Home & Hardware Hub",
                reason = "Incorrect part dimension",
                description = "Plumber brought 0.5 inch valve instead of 0.75 inch. Replaced next day.",
                status = DisputeStatus.RESOLVED,
                requestedRefundAmount = null,
                adminResolutionNotes = "Store owner visited and provided correct replacement part without extra charges.",
                createdAt = "4 days ago"
            )
        )
    )
    val disputes: StateFlow<List<MadeItDispute>> = _disputes.asStateFlow()

    fun addReview(storeId: String, storeName: String, customerName: String, rating: Int, comment: String) {
        val newReview = MadeItReview(
            storeId = storeId,
            storeName = storeName,
            customerName = customerName,
            rating = rating,
            comment = comment,
            createdAt = "Just now"
        )
        _reviews.value = listOf(newReview) + _reviews.value
    }

    fun addProviderReply(reviewId: String, reply: String) {
        _reviews.value = _reviews.value.map {
            if (it.id == reviewId) it.copy(providerReply = reply) else it
        }
    }

    fun raiseDispute(orderId: String, storeName: String, reason: String, description: String, refundAmount: Double?) {
        val newDispute = MadeItDispute(
            orderId = orderId,
            storeName = storeName,
            reason = reason,
            description = description,
            requestedRefundAmount = refundAmount,
            createdAt = "Just now"
        )
        _disputes.value = listOf(newDispute) + _disputes.value
    }

    fun resolveDispute(disputeId: String, resolutionNotes: String) {
        _disputes.value = _disputes.value.map {
            if (it.id == disputeId) it.copy(status = DisputeStatus.RESOLVED, adminResolutionNotes = resolutionNotes) else it
        }
    }
}
