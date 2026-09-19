package com.example.notifications

import java.util.UUID

enum class NotificationType(val label: String) {
    ORDER_UPDATE("Order Status"),
    BOOKING_CONFIRMED("Booking"),
    PAYMENT_SETTLED("Settlement"),
    DISPUTE_ALERT("Mediation"),
    PROMO("Local Offer")
}

data class MadeItNotification(
    val id: String = "NOTIF-${UUID.randomUUID().toString().substring(0, 6).uppercase()}",
    val title: String,
    val body: String,
    val type: NotificationType = NotificationType.ORDER_UPDATE,
    val timestamp: String = "Just now",
    val isRead: Boolean = false,
    val actionDeepLink: String? = null
)
