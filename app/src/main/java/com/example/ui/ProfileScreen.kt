package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.example.profile.BankPayoutDetails
import com.example.profile.CustomerAddress
import com.example.profile.ProfileViewModel
import com.example.profile.ProviderKycDocument
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: AuthUser,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addresses by profileViewModel.addresses.collectAsState()
    val documents by profileViewModel.documents.collectAsState()
    val bankDetails by profileViewModel.bankDetails.collectAsState()
    val isAvailable by profileViewModel.isAvailable.collectAsState()

    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showAddDocDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (user.role == UserRole.PROVIDER) "Provider Account & KYC" else "Customer Profile & Addresses",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.phone,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ProfileHeaderCard(user = user, isAvailable = isAvailable, onToggleAvailable = { profileViewModel.toggleAvailability() })
            }

            if (user.role == UserRole.CUSTOMER) {
                // Customer Section: Saved Addresses
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved Delivery & Service Addresses",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = { showAddAddressDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_address_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Address", fontSize = 12.sp)
                        }
                    }
                }

                items(addresses) { address ->
                    AddressCard(
                        address = address,
                        onSetDefault = { profileViewModel.setDefaultAddress(address.id) },
                        onDelete = { profileViewModel.deleteAddress(address.id) }
                    )
                }
            } else {
                // Provider Section: Verification Documents & Bank Account
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Business KYC Documents",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = { showAddDocDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("upload_doc_button")
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Document", fontSize = 12.sp)
                        }
                    }
                }

                items(documents) { doc ->
                    DocumentCard(doc = doc)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BankDetailsCard(
                        bankDetails = bankDetails,
                        onUpdate = { updated -> profileViewModel.updateBankDetails(updated) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            onDismiss = { showAddAddressDialog = false },
            onSave = { newAddr ->
                profileViewModel.addAddress(newAddr)
                showAddAddressDialog = false
            }
        )
    }

    if (showAddDocDialog) {
        AddDocumentDialog(
            onDismiss = { showAddDocDialog = false },
            onUpload = { type, num ->
                profileViewModel.uploadDocument(type, num)
                showAddDocDialog = false
            }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    user: AuthUser,
    isAvailable: Boolean,
    onToggleAvailable: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (user.role == UserRole.PROVIDER) AmberOrangeSoft else BrandBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (user.role == UserRole.PROVIDER) Icons.Filled.Storefront else Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = if (user.role == UserRole.PROVIDER) AmberOrangeDark else BrandBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(text = user.city + " • " + user.role.label, fontSize = 12.sp, color = TextSecondary)
                }
                Surface(
                    color = if (user.isVerified) EmeraldGreenSoft else AmberOrangeSoft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (user.isVerified) "VERIFIED" else "PENDING",
                        color = if (user.isVerified) EmeraldGreen else AmberOrangeDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (user.role == UserRole.PROVIDER) {
                HorizontalDivider(color = BorderLight)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Business Accepting Requests", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isAvailable) "Visible in search & discovery" else "Temporarily offline",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { onToggleAvailable() },
                        modifier = Modifier.testTag("availability_switch")
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressCard(
    address: CustomerAddress,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (address.isDefault) 1.5.dp else 1.dp,
            color = if (address.isDefault) BrandBlue else BorderLight
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (address.label == "Home") Icons.Filled.Home else Icons.Filled.Work,
                        contentDescription = "Label",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = address.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (address.isDefault) {
                    Surface(color = BrandBlueSoft, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "DEFAULT",
                            color = BrandBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(text = address.addressLine1, fontSize = 13.sp, color = TextPrimary)
            if (!address.landmark.isNullOrBlank()) {
                Text(text = "Landmark: " + address.landmark, fontSize = 12.sp, color = TextSecondary)
            }
            Text(text = "${address.area}, ${address.city} - ${address.pincode}", fontSize = 12.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!address.isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Text("Set as Default", fontSize = 12.sp, color = BrandBlue)
                    }
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(doc: ProviderKycDocument) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberOrangeSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Badge, contentDescription = "KYC", tint = AmberOrangeDark, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = doc.documentType, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Number: ${doc.documentNumber}", fontSize = 12.sp, color = TextSecondary)
                if (doc.remarks != null) {
                    Text(text = doc.remarks, fontSize = 11.sp, color = TextMuted)
                }
            }
            Surface(
                color = when (doc.status) {
                    "VERIFIED" -> EmeraldGreenSoft
                    "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                    else -> AmberOrangeSoft
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = doc.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (doc.status) {
                        "VERIFIED" -> EmeraldGreen
                        "REJECTED" -> MaterialTheme.colorScheme.error
                        else -> AmberOrangeDark
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun BankDetailsCard(
    bankDetails: BankPayoutDetails,
    onUpdate: (BankPayoutDetails) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.AccountBalance, contentDescription = "Bank", tint = BrandBlue)
                Text("Settlement & Payout Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("Payments from completed customer orders and services will be credited to this verified account.", fontSize = 11.sp, color = TextSecondary)

            Surface(
                color = SurfaceLight,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "A/C Holder: ${bankDetails.bankAccountName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(text = "Account: •••• •••• ${bankDetails.bankAccountNumber.takeLast(4)}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "IFSC: ${bankDetails.bankIfscCode}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "UPI VPA: ${bankDetails.upiId}", fontSize = 12.sp, color = BrandBlue, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AddAddressDialog(
    onDismiss: () -> Unit,
    onSave: (CustomerAddress) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var address1 by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("560001") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Address Label (Home / Work)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address1,
                    onValueChange = { address1 = it },
                    label = { Text("House / Flat / Street") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Locality") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Pincode") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address1.isNotBlank() && area.isNotBlank()) {
                        onSave(
                            CustomerAddress(
                                label = label,
                                contactName = "Customer",
                                contactPhone = "+919876543210",
                                addressLine1 = address1,
                                landmark = landmark,
                                area = area,
                                city = "Bengaluru",
                                pincode = pincode,
                                isDefault = false
                            )
                        )
                    }
                }
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddDocumentDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String) -> Unit
) {
    var docType by remember { mutableStateOf("PAN CARD") }
    var docNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit KYC Document", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = docType,
                    onValueChange = { docType = it },
                    label = { Text("Document Type (PAN / GST / AADHAAR)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text("Document Identification Number") },
                    singleLine = true
                )
                Text(
                    text = "Uploads are verified by the Made It Super Admin compliance desk within 24 hours.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (docNumber.isNotBlank()) {
                        onUpload(docType, docNumber)
                    }
                }
            ) {
                Text("Submit for KYC")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
