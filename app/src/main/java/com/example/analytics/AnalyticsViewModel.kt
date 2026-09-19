package com.example.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnalyticsViewModel : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    private val _providerAnalytics = MutableStateFlow(ProviderAnalytics())
    val providerAnalytics: StateFlow<ProviderAnalytics> = _providerAnalytics.asStateFlow()

    private val _customerAnalytics = MutableStateFlow(CustomerSpendingAnalytics())
    val customerAnalytics: StateFlow<CustomerSpendingAnalytics> = _customerAnalytics.asStateFlow()

    private val _adminAnalytics = MutableStateFlow(AdminPlatformAnalytics())
    val adminAnalytics: StateFlow<AdminPlatformAnalytics> = _adminAnalytics.asStateFlow()

    private val _coupons = MutableStateFlow(
        listOf(
            PromoCoupon("MADEIT20", "20% OFF on First Local Order", 20, 299.0, 100.0, "31 Oct 2026"),
            PromoCoupon("FREEDELIVERY", "Free Delivery on Groceries over ₹499", 100, 499.0, 40.0, "15 Nov 2026"),
            PromoCoupon("LOCALBIZ50", "Flat ₹50 OFF on Home Service Visits", 15, 399.0, 50.0, "30 Nov 2026"),
            PromoCoupon("DIWALIDISCOUNT", "Festive 10% Cashback on All Stores", 10, 599.0, 150.0, "05 Nov 2026")
        )
    )
    val coupons: StateFlow<List<PromoCoupon>> = _coupons.asStateFlow()

    fun selectPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
        // Adjust simulated multiplier according to selected timeframe
        val multiplier = when (period) {
            AnalyticsPeriod.TODAY -> 0.15
            AnalyticsPeriod.WEEK -> 1.0
            AnalyticsPeriod.MONTH -> 4.2
            AnalyticsPeriod.YEAR -> 48.0
        }
        _providerAnalytics.update { current ->
            current.copy(
                grossRevenue = 48250.0 * multiplier,
                netEarnings = 45837.5 * multiplier,
                platformFeePaid = 2412.5 * multiplier,
                totalOrders = (84 * multiplier).toInt()
            )
        }
    }

    fun addCoupon(
        code: String,
        title: String,
        discountPercent: Int,
        minOrder: Double,
        maxDiscount: Double,
        expiry: String
    ) {
        val newCoupon = PromoCoupon(
            code = code.uppercase().trim(),
            title = title.trim(),
            discountPercent = discountPercent,
            minOrderAmount = minOrder,
            maxDiscountAmount = maxDiscount,
            expiryDate = expiry,
            isActive = true
        )
        _coupons.update { listOf(newCoupon) + it }
    }
}
