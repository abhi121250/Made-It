package com.example.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val _orders = MutableStateFlow<List<MadeItOrder>>(
        listOf(
            MadeItOrder(
                id = "ORD-89F12A01",
                type = OrderType.ON_DEMAND_SERVICE,
                status = OrderStatus.ACCEPTED,
                storeName = "Kumar Home & Hardware Hub",
                customerName = "Ramesh Kumar",
                customerPhone = "+919876543210",
                deliveryAddress = "#42, 3rd Cross, Indiranagar, Bengaluru",
                items = listOf(
                    OrderItem(
                        title = "Emergency Tap & Pipe Leak Repair",
                        quantity = 1,
                        unitPrice = 249.0,
                        totalPrice = 249.0
                    )
                ),
                subtotal = 249.0,
                deliveryFee = 0.0,
                totalAmount = 249.0,
                scheduledTime = "Today at 04:30 PM",
                paymentMethod = "Pay after visit via UPI",
                createdAt = "10 mins ago"
            ),
            MadeItOrder(
                id = "ORD-44BC719D",
                type = OrderType.RETAIL_DELIVERY,
                status = OrderStatus.PENDING,
                storeName = "Kumar Home & Hardware Hub",
                customerName = "Ramesh Kumar",
                customerPhone = "+919876543210",
                deliveryAddress = "#42, 3rd Cross, Indiranagar, Bengaluru",
                items = listOf(
                    OrderItem(
                        title = "Heavy-Duty Brass Angle Valve (1/2\")",
                        quantity = 2,
                        unitPrice = 380.0,
                        totalPrice = 760.0
                    ),
                    OrderItem(
                        title = "Havells 10W Cool Day LED Bulb (B22)",
                        quantity = 4,
                        unitPrice = 110.0,
                        totalPrice = 440.0
                    )
                ),
                subtotal = 1200.0,
                deliveryFee = 40.0,
                totalAmount = 1240.0,
                scheduledTime = "Instant 45-min delivery",
                paymentMethod = "Cash on Delivery",
                createdAt = "25 mins ago"
            ),
            MadeItOrder(
                id = "ORD-12FE56A2",
                type = OrderType.STORE_PICKUP,
                status = OrderStatus.COMPLETED,
                storeName = "Kumar Home & Hardware Hub",
                customerName = "Priya Sundaram",
                customerPhone = "+919844433221",
                deliveryAddress = "Store Counter Pickup - Indiranagar",
                items = listOf(
                    OrderItem(
                        title = "Water Heater / Geyser Service",
                        quantity = 1,
                        unitPrice = 449.0,
                        totalPrice = 449.0
                    )
                ),
                subtotal = 449.0,
                deliveryFee = 0.0,
                totalAmount = 449.0,
                scheduledTime = "Completed Yesterday",
                paymentMethod = "UPI QR Scan",
                createdAt = "Yesterday"
            )
        )
    )
    val orders: StateFlow<List<MadeItOrder>> = _orders.asStateFlow()

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map { order ->
            if (order.id == orderId) order.copy(status = newStatus) else order
        }
    }

    fun placeOrder(newOrder: MadeItOrder) {
        _orders.value = listOf(newOrder) + _orders.value
    }
}
