package com.example.profile

import java.util.UUID

data class CustomerAddress(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // Home, Office, Work
    val contactName: String,
    val contactPhone: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val landmark: String? = null,
    val area: String,
    val city: String,
    val state: String = "Karnataka",
    val pincode: String,
    val isDefault: Boolean = false
)

data class ProviderKycDocument(
    val id: String = UUID.randomUUID().toString(),
    val documentType: String, // AADHAAR, PAN, GST, TRADE_LICENSE
    val documentNumber: String,
    val fileUrl: String,
    val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val remarks: String? = null
)

data class BankPayoutDetails(
    val bankAccountNumber: String = "",
    val bankIfscCode: String = "",
    val bankAccountName: String = "",
    val upiId: String = ""
)
