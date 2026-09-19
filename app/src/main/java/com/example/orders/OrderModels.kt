package com.example.orders

import java.util.UUID

enum class OrderType {
    RETAIL_DELIVERY,
    STORE_PICKUP,
    ON_DEMAND_SERVICE
}

enum class OrderStatus(val label: String) {
    PENDING("Pending Confirmation"),
    ACCEPTED("Accepted / Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

data class OrderItem(
    val title: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

data class MadeItOrder(
    val id: String = "ORD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
    val type: OrderType,
    val status: OrderStatus,
    val storeName: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double,
    val scheduledTime: String = "Today, within 45 mins",
    val paymentMethod: String = "Cash on Delivery / UPI",
    val createdAt: String = "Just now"
)
