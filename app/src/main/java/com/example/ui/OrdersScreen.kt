package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.example.orders.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    user: AuthUser,
    ordersViewModel: OrdersViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by ordersViewModel.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> orders.filter { it.status == OrderStatus.PENDING }
            "ACCEPTED" -> orders.filter { it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.IN_PROGRESS }
            "COMPLETED" -> orders.filter { it.status == OrderStatus.COMPLETED }
            else -> orders
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (user.role == UserRole.PROVIDER) "Store Orders & Bookings" else "My Orders & Service Visits",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${orders.size} Total Transactions",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("orders_back_button")) {
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
                // Status Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${orders.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("Pending", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "ACCEPTED",
                        onClick = { selectedFilter = "ACCEPTED" },
                        label = { Text("Active", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "COMPLETED",
                        onClick = { selectedFilter = "COMPLETED" },
                        label = { Text("Completed", fontSize = 12.sp) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.ReceiptLong, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                            Text("No orders in this status", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredOrders) { order ->
                    OrderCard(
                        order = order,
                        isProvider = user.role == UserRole.PROVIDER,
                        onUpdateStatus = { newStatus -> ordersViewModel.updateOrderStatus(order.id, newStatus) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: MadeItOrder,
    isProvider: Boolean,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Order ID and Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${order.type.name.replace("_", " ")} • ${order.createdAt}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                val (badgeBg, badgeText) = when (order.status) {
                    OrderStatus.PENDING -> AmberOrangeSoft to AmberOrangeDark
                    OrderStatus.ACCEPTED -> BrandBlueSoft to BrandBlue
                    OrderStatus.IN_PROGRESS -> Color(0xFFEDE9FE) to Color(0xFF7C3AED)
                    OrderStatus.COMPLETED -> EmeraldGreenSoft to EmeraldGreen
                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.status.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderLight)

            // Items breakdown
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₹${item.totalPrice.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Delivery & Schedule notes
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Time", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(text = "Schedule: ${order.scheduledTime}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(text = order.deliveryAddress, fontSize = 11.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Payment, contentDescription = "Payment", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(text = "Payment: ${order.paymentMethod}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            HorizontalDivider(color = BorderLight)

            // Total amount & Provider action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Payable", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "₹${order.totalAmount.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isProvider) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (order.status == OrderStatus.PENDING) {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Decline", fontSize = 12.sp)
                            }
                        } else if (order.status == OrderStatus.ACCEPTED) {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.IN_PROGRESS) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Start Visit / Pack", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (order.status == OrderStatus.IN_PROGRESS) {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Complete Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { /* Customer contact provider */ },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Store", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
