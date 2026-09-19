package com.example.payments

import java.util.UUID

enum class PaymentMode(val label: String) {
    UPI_INTENT("UPI (GPay / PhonePe / Paytm)"),
    RAZORPAY_CARD("Debit / Credit Card / NetBanking"),
    CASH_ON_DELIVERY("Cash On Delivery / Post-Service Visit")
}

enum class PaymentStatus(val label: String) {
    INITIATED("Initiated"),
    SUCCESS("Payment Completed"),
    FAILED("Payment Failed"),
    REFUNDED("Refunded")
}

data class MadeItPayment(
    val id: String = "PAY-${UUID.randomUUID().toString().substring(0, 8).uppercase()}",
    val orderId: String,
    val amount: Double,
    val mode: PaymentMode,
    val status: PaymentStatus,
    val gatewayTransactionId: String? = null,
    val providerPayoutAmount: Double,
    val platformFee: Double,
    val createdAt: String = "Just now"
)

data class ProviderSettlementSummary(
    val totalEarnings: Double = 18450.0,
    val pendingPayout: Double = 3420.0,
    val settledPayout: Double = 15030.0,
    val linkedUpiId: String = "kumarplumbing@okhdfcbank",
    val linkedBankAccount: String = "•••• •••• 1298 (HDFC0001234)",
    val nextSettlementDate: String = "Tomorrow, 10:00 AM (Daily T+1 cycle)"
)
