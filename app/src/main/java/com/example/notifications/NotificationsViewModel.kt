package com.example.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _notifications = MutableStateFlow<List<MadeItNotification>>(
        listOf(
            MadeItNotification(
                id = "NOTIF-001",
                title = "Order Accepted: Tap Leak Repair",
                body = "Kumar Home & Hardware Hub has accepted your booking for today at 3:30 PM. Plumber Rajesh is assigned.",
                type = NotificationType.BOOKING_CONFIRMED,
                timestamp = "10 mins ago",
                isRead = false
            ),
            MadeItNotification(
                id = "NOTIF-002",
                title = "Payment Settled: ₹1,850 Payout",
                body = "Daily T+1 settlement of ₹1,850 has been disbursed to your linked UPI ID (kumarhardware@icici).",
                type = NotificationType.PAYMENT_SETTLED,
                timestamp = "2 hours ago",
                isRead = false
            ),
            MadeItNotification(
                id = "NOTIF-003",
                title = "Customer Left a 5-Star Review! ⭐",
                body = "Rohan Sharma praised your prompt visit: 'Arrived on time within 30 minutes. Fixed water tap leak cleanly!'",
                type = NotificationType.ORDER_UPDATE,
                timestamp = "Yesterday",
                isRead = true
            ),
            MadeItNotification(
                id = "NOTIF-004",
                title = "Dispute Resolved: ORD-12FE56A2",
                body = "The moderation team at SB Creatives marked dispute DSP-0812 as resolved following part replacement.",
                type = NotificationType.DISPUTE_ALERT,
                timestamp = "2 days ago",
                isRead = true
            )
        )
    )
    val notifications: StateFlow<List<MadeItNotification>> = _notifications.asStateFlow()

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    fun dismissNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
    }
}
