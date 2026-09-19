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
import com.example.payments.MadeItPayment
import com.example.payments.PaymentMode
import com.example.payments.PaymentStatus
import com.example.payments.PaymentsViewModel
import com.example.payments.ProviderSettlementSummary
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    user: AuthUser,
    paymentsViewModel: PaymentsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settlement by paymentsViewModel.settlementSummary.collectAsState()
    val transactions by paymentsViewModel.payments.collectAsState()

    var showPayoutSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (user.role == UserRole.PROVIDER) "Settlement & Payout Hub" else "Payments & Receipts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MADE IT Escrow & Settlement Architecture",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("payments_back_button")) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (user.role == UserRole.PROVIDER || user.role == UserRole.ADMIN) {
                // Provider Settlement Dashboard Banner
                item {
                    ProviderEarningsCard(
                        settlement = settlement,
                        onRequestPayout = {
                            paymentsViewModel.requestInstantPayout()
                            showPayoutSuccessDialog = true
                        }
                    )
                }
            }

            // Gateway & Payment Options Card
            item {
                GatewayIntegrationsCard()
            }

            // Transaction History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions (${transactions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "5% Platform Fee Auto-Deducted",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            items(transactions) { tx ->
                TransactionRowCard(payment = tx, isProvider = user.role == UserRole.PROVIDER)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPayoutSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPayoutSuccessDialog = false },
            icon = { Icon(Icons.Filled.Verified, contentDescription = "Success", tint = EmeraldGreen, modifier = Modifier.size(36.dp)) },
            title = { Text("Payout Requested Successfully", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your balance of ₹3,420 has been queued for payout to ${settlement.linkedUpiId} (${settlement.linkedBankAccount}). Funds will reflect within 15 minutes."
                )
            },
            confirmButton = {
                Button(onClick = { showPayoutSuccessDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun ProviderEarningsCard(
    settlement: ProviderSettlementSummary,
    onRequestPayout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pending Settlement Balance",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${settlement.pendingPayout.toInt()}",
                        color = AmberOrange,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Button(
                    onClick = onRequestPayout,
                    enabled = settlement.pendingPayout > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberOrange, contentColor = Navy900),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("Instant Payout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total Lifetime Earnings", color = TextMuted, fontSize = 11.sp)
                    Text(text = "₹${settlement.totalEarnings.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Already Settled", color = TextMuted, fontSize = 11.sp)
                    Text(text = "₹${settlement.settledPayout.toInt()}", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = "Bank", tint = BrandBlue, modifier = Modifier.size(16.dp))
                    Column {
                        Text(text = "Payout Target: ${settlement.linkedUpiId}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(text = settlement.nextSettlementDate, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun GatewayIntegrationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Supported Instant Payment Channels", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PaymentChip("UPI QR & Apps", Icons.Filled.QrCode2, BrandBlue)
                PaymentChip("Cards / NetBanking", Icons.Filled.CreditCard, EmeraldGreen)
                PaymentChip("Cash on Delivery", Icons.Filled.Money, AmberOrangeDark)
            }
        }
    }
}

@Composable
private fun PaymentChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Surface(
        color = tint.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

@Composable
private fun TransactionRowCard(payment: MadeItPayment, isProvider: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (payment.status == PaymentStatus.SUCCESS) EmeraldGreenSoft else AmberOrangeSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (payment.mode == PaymentMode.CASH_ON_DELIVERY) Icons.Filled.Money else Icons.Filled.Payments,
                        contentDescription = "Payment",
                        tint = if (payment.status == PaymentStatus.SUCCESS) EmeraldGreen else AmberOrangeDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = payment.orderId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${payment.mode.label} • ${payment.createdAt}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (payment.gatewayTransactionId != null) {
                        Text(
                            text = "Ref: ${payment.gatewayTransactionId}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${payment.amount.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isProvider) {
                    Text(
                        text = "Net: ₹${payment.providerPayoutAmount.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreen
                    )
                }
            }
        }
    }
}
