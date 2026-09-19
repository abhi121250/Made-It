package com.example.analytics

import java.util.Date

enum class AnalyticsPeriod(val label: String) {
    TODAY("Today"),
    WEEK("7 Days"),
    MONTH("30 Days"),
    YEAR("This Year")
}

data class DailySalesData(
    val dayLabel: String,
    val amount: Double,
    val ordersCount: Int
)

data class TopSellingItem(
    val id: String,
    val name: String,
    val category: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val isService: Boolean = false
)

data class ProviderAnalytics(
    val grossRevenue: Double = 48250.0,
    val netEarnings: Double = 45837.5, // 95% after 5% platform commission
    val platformFeePaid: Double = 2412.5,
    val totalOrders: Int = 84,
    val averageOrderValue: Double = 574.4,
    val conversionRate: Double = 68.5,
    val profileImpressions: Int = 1240,
    val repeatCustomerRate: Double = 42.0,
    val salesHistory: List<DailySalesData> = listOf(
        DailySalesData("Mon", 5200.0, 9),
        DailySalesData("Tue", 6800.0, 12),
        DailySalesData("Wed", 4900.0, 8),
        DailySalesData("Thu", 7400.0, 14),
        DailySalesData("Fri", 8900.0, 16),
        DailySalesData("Sat", 11250.0, 20),
        DailySalesData("Sun", 3800.0, 5)
    ),
    val topItems: List<TopSellingItem> = listOf(
        TopSellingItem("p1", "Aashirvaad Superior MP Sharbati Atta 10kg", "Groceries", 34, 18700.0),
        TopSellingItem("s1", "AC Jet Cleaning & Gas Leakage Diagnostic", "AC & Appliance Repair", 14, 13986.0, true),
        TopSellingItem("p2", "Fortune Sunlite Refined Sunflower Oil 5L", "Groceries", 28, 5544.0),
        TopSellingItem("s2", "Emergency Circuit Breaker / Switchboard Repair", "Electrical Repair", 11, 3839.0, true),
        TopSellingItem("p3", "Tata Tea Gold 1kg Fresh Pack", "Groceries", 19, 11400.0)
    ),
    val peakHoursSummary: String = "Peak order volume occurs between 5:00 PM - 8:30 PM (48% of daily sales).",
    val localDeliverySpeedMins: Int = 24
)

data class CustomerSpendingAnalytics(
    val totalSpent: Double = 14850.0,
    val totalOrdersCount: Int = 18,
    val localSavingsAmount: Double = 1940.0,
    val co2SavedKg: Double = 14.6,
    val neighborhoodStoresSupported: Int = 6,
    val loyaltyRewardPoints: Int = 480,
    val categorySpendBreakdown: Map<String, Double> = mapOf(
        "Groceries & Essentials" to 8420.0,
        "Home Electrician & Plumbing" to 3450.0,
        "Fresh Fruits & Vegetables" to 1980.0,
        "Local Sweets & Bakery" to 1000.0
    )
)

data class AdminPlatformAnalytics(
    val grossMerchandiseValue: Double = 342800.0,
    val totalPlatformCommission: Double = 17140.0, // 5% fee
    val totalOrdersProcessed: Int = 612,
    val activeMerchants: Int = 48,
    val verifiedMerchantsPercentage: Double = 91.6,
    val averageResolutionTimeHours: Double = 1.8,
    val platformOrderSuccessRate: Double = 97.4,
    val categoryGmv: Map<String, Double> = mapOf(
        "Groceries & Supermarkets" to 142000.0,
        "AC, Electrical & Plumbing Services" to 89400.0,
        "Electronics & Mobile Repairs" to 58600.0,
        "Hardware & Sanitary" to 34800.0,
        "Sweets, Dairy & Bakeries" to 18000.0
    )
)

data class PromoCoupon(
    val code: String,
    val title: String,
    val discountPercent: Int,
    val minOrderAmount: Double,
    val maxDiscountAmount: Double,
    val expiryDate: String,
    val isActive: Boolean = true
)
