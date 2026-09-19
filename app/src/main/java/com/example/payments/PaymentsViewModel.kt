package com.example.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val _settlementSummary = MutableStateFlow(ProviderSettlementSummary())
    val settlementSummary: StateFlow<ProviderSettlementSummary> = _settlementSummary.asStateFlow()

    private val _payments = MutableStateFlow<List<MadeItPayment>>(
        listOf(
            MadeItPayment(
                id = "PAY-9182AC01",
                orderId = "ORD-89F12A01",
                amount = 249.0,
                mode = PaymentMode.UPI_INTENT,
                status = PaymentStatus.SUCCESS,
                gatewayTransactionId = "pay_rzp_91028340129",
                providerPayoutAmount = 236.55,
                platformFee = 12.45,
                createdAt = "10 mins ago"
            ),
            MadeItPayment(
                id = "PAY-7721EF89",
                orderId = "ORD-44BC719D",
                amount = 1240.0,
                mode = PaymentMode.CASH_ON_DELIVERY,
                status = PaymentStatus.INITIATED,
                gatewayTransactionId = "cod_pending_collect",
                providerPayoutAmount = 1178.00,
                platformFee = 62.00,
                createdAt = "25 mins ago"
            ),
            MadeItPayment(
                id = "PAY-3310BB12",
                orderId = "ORD-12FE56A2",
                amount = 449.0,
                mode = PaymentMode.UPI_INTENT,
                status = PaymentStatus.SUCCESS,
                gatewayTransactionId = "upi_tx_492019381023",
                providerPayoutAmount = 426.55,
                platformFee = 22.45,
                createdAt = "Yesterday"
            )
        )
    )
    val payments: StateFlow<List<MadeItPayment>> = _payments.asStateFlow()

    fun requestInstantPayout() {
        val current = _settlementSummary.value
        _settlementSummary.value = current.copy(
            settledPayout = current.settledPayout + current.pendingPayout,
            pendingPayout = 0.0
        )
    }
}
