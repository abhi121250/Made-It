package com.example.catalog

import java.util.UUID

enum class ItemType {
    PRODUCT,
    SERVICE
}

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val iconName: String,
    val isService: Boolean
)

data class Store(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val slug: String,
    val description: String,
    val phone: String,
    val address: String,
    val area: String,
    val city: String = "Bengaluru",
    val rating: Double = 4.8,
    val totalReviews: Int = 34,
    val isAcceptingOrders: Boolean = true,
    val openingTime: String = "09:00 AM",
    val closingTime: String = "09:00 PM",
    val bannerUrl: String? = null
)

data class CatalogItem(
    val id: String = UUID.randomUUID().toString(),
    val storeId: String,
    val categoryId: String,
    val type: ItemType,
    val title: String,
    val description: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val unit: String = "per item", // "per hour", "visit charge", "kg", "pack"
    val inStock: Boolean = true,
    val stockQuantity: Int = 20,
    val durationMinutes: Int? = null,
    val imageUrl: String? = null
)
