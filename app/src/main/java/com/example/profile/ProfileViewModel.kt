package com.example.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    // Customer Addresses state
    private val _addresses = MutableStateFlow<List<CustomerAddress>>(
        listOf(
            CustomerAddress(
                id = "addr_1",
                label = "Home",
                contactName = "Ramesh Kumar",
                contactPhone = "+919876543210",
                addressLine1 = "#42, 3rd Cross, Indiranagar",
                landmark = "Near Metro Station",
                area = "Indiranagar",
                city = "Bengaluru",
                pincode = "560038",
                isDefault = true
            ),
            CustomerAddress(
                id = "addr_2",
                label = "Office",
                contactName = "Ramesh Kumar",
                contactPhone = "+919876543210",
                addressLine1 = "Tech Park, Tower B, 4th Floor",
                area = "Whitefield",
                city = "Bengaluru",
                pincode = "560066",
                isDefault = false
            )
        )
    )
    val addresses: StateFlow<List<CustomerAddress>> = _addresses.asStateFlow()

    // Provider KYC Documents state
    private val _documents = MutableStateFlow<List<ProviderKycDocument>>(
        listOf(
            ProviderKycDocument(
                id = "doc_1",
                documentType = "AADHAAR",
                documentNumber = "XXXX-XXXX-4589",
                fileUrl = "https://madeit.sbcreatives.in/docs/aadhaar_preview.jpg",
                status = "VERIFIED",
                remarks = "Identity verified via UIDAI"
            ),
            ProviderKycDocument(
                id = "doc_2",
                documentType = "GST / TRADE LICENSE",
                documentNumber = "29ABCDE1234F1Z5",
                fileUrl = "https://madeit.sbcreatives.in/docs/gst_cert.pdf",
                status = "UNDER_REVIEW",
                remarks = "Verification under review by Made It Admin"
            )
        )
    )
    val documents: StateFlow<List<ProviderKycDocument>> = _documents.asStateFlow()

    // Provider Bank Details state
    private val _bankDetails = MutableStateFlow(
        BankPayoutDetails(
            bankAccountNumber = "918237461298",
            bankIfscCode = "HDFC0001234",
            bankAccountName = "Kumar Plumbing Solutions",
            upiId = "kumarplumbing@okhdfcbank"
        )
    )
    val bankDetails: StateFlow<BankPayoutDetails> = _bankDetails.asStateFlow()

    // Provider Availability Toggle
    private val _isAvailable = MutableStateFlow(true)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    fun toggleAvailability() {
        _isAvailable.value = !_isAvailable.value
    }

    fun addAddress(address: CustomerAddress) {
        val updated = _addresses.value.toMutableList()
        if (address.isDefault) {
            updated.indices.forEach { i ->
                updated[i] = updated[i].copy(isDefault = false)
            }
        }
        updated.add(address)
        _addresses.value = updated
    }

    fun setDefaultAddress(id: String) {
        _addresses.value = _addresses.value.map { addr ->
            addr.copy(isDefault = addr.id == id)
        }
    }

    fun deleteAddress(id: String) {
        _addresses.value = _addresses.value.filter { it.id != id }
    }

    fun updateBankDetails(bankDetails: BankPayoutDetails) {
        _bankDetails.value = bankDetails
    }

    fun uploadDocument(type: String, number: String) {
        val newDoc = ProviderKycDocument(
            id = UUID.randomUUID().toString(),
            documentType = type,
            documentNumber = number,
            fileUrl = "https://madeit.sbcreatives.in/uploads/doc_${System.currentTimeMillis()}.jpg",
            status = "PENDING",
            remarks = "Submitted for verification"
        )
        _documents.value = _documents.value + newDoc
    }
}
