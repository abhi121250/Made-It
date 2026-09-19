package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.analytics.*
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    user: AuthUser,
    analyticsViewModel: AnalyticsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPeriod by analyticsViewModel.selectedPeriod.collectAsState()
    val providerAnalytics by analyticsViewModel.providerAnalytics.collectAsState()
    val customerAnalytics by analyticsViewModel.customerAnalytics.collectAsState()
    val adminAnalytics by analyticsViewModel.adminAnalytics.collectAsState()
    val coupons by analyticsViewModel.coupons.collectAsState()

    var showCreateCouponDialog by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (user.role) {
                                UserRole.PROVIDER -> "Store Analytics & Growth Hub"
                                UserRole.CUSTOMER -> "My Local Spend & Impact"
                                UserRole.ADMIN -> "Platform BI & GMV Analytics"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MADE IT Business Intelligence Engine",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("analytics_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (user.role == UserRole.PROVIDER) {
                        IconButton(
                            onClick = { showCreateCouponDialog = true },
                            modifier = Modifier.testTag("add_promo_code_button")
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = "Add Coupon", tint = BrandBlue)
                        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Timeframe Selector Chips for Provider & Admin
            if (user.role != UserRole.CUSTOMER) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsPeriod.values().forEach { period ->
                            val isSelected = selectedPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { analyticsViewModel.selectPeriod(period) },
                                label = { Text(period.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Notice Banner if exported
            exportSuccessMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldGreenSoft),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                            Text(msg, fontSize = 13.sp, color = Navy900, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Role-Specific Sections
            when (user.role) {
                UserRole.PROVIDER -> {
                    item {
                        ProviderRevenueOverview(providerAnalytics)
                    }
                    item {
                        SalesVelocityBarChart(providerAnalytics.salesHistory)
                    }
                    item {
                        ProviderStoreMetricsGrid(providerAnalytics)
                    }
                    item {
                        TopSellingProductsSection(providerAnalytics.topItems)
                    }
                    item {
                        GstComplianceCard(
                            onExportGst = {
                                exportSuccessMessage = "Monthly GST & Commission Statement downloaded (PDF/Excel generated)."
                            }
                        )
                    }
                    item {
                        PromotionsManagerSection(
                            coupons = coupons,
                            onAddNew = { showCreateCouponDialog = true }
                        )
                    }
                }
                UserRole.CUSTOMER -> {
                    item {
                        CustomerSpendHeroCard(customerAnalytics)
                    }
                    item {
                        CustomerEcoImpactCard(customerAnalytics)
                    }
                    item {
                        CustomerCategorySpendBreakdown(customerAnalytics.categorySpendBreakdown)
                    }
                    item {
                        ActiveNeighborhoodDeals(coupons)
                    }
                }
                UserRole.ADMIN -> {
                    item {
                        AdminPlatformGmvCard(adminAnalytics)
                    }
                    item {
                        AdminEcosystemHealthGrid(adminAnalytics)
                    }
                    item {
                        AdminCategoryVolumeDistribution(adminAnalytics.categoryGmv)
                    }
                    item {
                        PromotionsManagerSection(
                            coupons = coupons,
                            onAddNew = { showCreateCouponDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showCreateCouponDialog) {
        CreateCouponDialog(
            onDismiss = { showCreateCouponDialog = false },
            onConfirm = { code, title, discount, minOrder, maxDisc, expiry ->
                analyticsViewModel.addCoupon(code, title, discount, minOrder, maxDisc, expiry)
                showCreateCouponDialog = false
            }
        )
    }
}

// ---------------- PROVIDER COMPOSABLES ----------------

@Composable
private fun ProviderRevenueOverview(data: ProviderAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL GROSS SALES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
                    Text(
                        "₹${String.format("%,.2f", data.grossRevenue)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("+18.4% vs last period", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Navy700)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Net Disbursed (95%)", fontSize = 11.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", data.netEarnings)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
                Column {
                    Text("Made It Fee (5%)", fontSize = 11.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", data.platformFeePaid)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
                }
                Column {
                    Text("Total Orders", fontSize = 11.sp, color = TextMuted)
                    Text("${data.totalOrders} orders", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SalesVelocityBarChart(history: List<DailySalesData>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
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
                Text("Sales Trend & Order Volume", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Weekly Velocity", fontSize = 12.sp, color = BrandBlue, fontWeight = FontWeight.SemiBold)
            }

            val maxAmount = (history.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                history.forEach { item ->
                    val fraction = (item.amount / maxAmount).toFloat().coerceIn(0.1f, 1.0f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${(item.amount / 1000).toInt()}k",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((fraction * 90).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (item.dayLabel == "Sat") AmberOrange else BrandBlue)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.dayLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderStoreMetricsGrid(data: ProviderAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Store Performance Indicators", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Avg Ticket Size",
                value = "₹${data.averageOrderValue.toInt()}",
                subtitle = "Per checkout",
                icon = Icons.Filled.ShoppingBag,
                iconColor = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Conversion Rate",
                value = "${data.conversionRate}%",
                subtitle = "Visits to purchases",
                icon = Icons.Filled.TrendingUp,
                iconColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Repeat Shoppers",
                value = "${data.repeatCustomerRate}%",
                subtitle = "Neighborhood loyalty",
                icon = Icons.Filled.Repeat,
                iconColor = AmberOrangeDark,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Fulfilment Speed",
                value = "${data.localDeliverySpeedMins} mins",
                subtitle = "Avg doorstep time",
                icon = Icons.Filled.Timer,
                iconColor = BrandBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 12.sp, color = TextSecondary)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun TopSellingProductsSection(items: List<TopSellingItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Top Selling Items & Booked Services", fontSize = 15.sp, fontWeight = FontWeight.Bold)

            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) AmberOrangeSoft else BrandBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "#${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index == 0) AmberOrangeDark else BrandBlue
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            text = "${item.unitsSold} ${if (item.isService) "visits completed" else "units sold"} • ${item.category}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Text(
                        "₹${String.format("%,.0f", item.totalRevenue)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = BorderLight)
                }
            }
        }
    }
}

@Composable
private fun GstComplianceCard(onExportGst: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = BrandBlue)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("GST Sales Statement & Tax Filing", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Export ready-to-file statement of gross sales, TCS & 5% commission", fontSize = 11.sp, color = TextSecondary)
            }
            Button(
                onClick = onExportGst,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- CUSTOMER COMPOSABLES ----------------

@Composable
private fun CustomerSpendHeroCard(data: CustomerSpendingAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandBlue)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("MY NEIGHBORHOOD SPEND", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberOrangeSoft)
            Text(
                "₹${String.format("%,.2f", data.totalSpent)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            HorizontalDivider(color = BrandBlueLight.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Deals Savings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("₹${data.localSavingsAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenSoft)
                }
                Column {
                    Text("Local Shops Empowered", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("${data.neighborhoodStoresSupported} stores", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Loyalty Coins", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("${data.loyaltyRewardPoints} pts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
                }
            }
        }
    }
}

@Composable
private fun CustomerEcoImpactCard(data: CustomerSpendingAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldGreenSoft),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.Eco, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(36.dp))
            Column {
                Text("Hyperlocal Green Impact", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy900)
                Text(
                    "You prevented ~${data.co2SavedKg} kg CO2 emissions by procuring orders from neighborhood stores within 2 km instead of long-distance central warehouses.",
                    fontSize = 12.sp,
                    color = Navy800
                )
            }
        }
    }
}

@Composable
private fun CustomerCategorySpendBreakdown(categories: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Category Spend Distribution", fontSize = 15.sp, fontWeight = FontWeight.Bold)

            val total = categories.values.sum().coerceAtLeast(1.0)

            categories.forEach { (cat, amount) ->
                val fraction = (amount / total).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("₹${amount.toInt()} (${(fraction * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = BrandBlue,
                        trackColor = BorderLight
                    )
                }
            }
        }
    }
}

// ---------------- ADMIN COMPOSABLES ----------------

@Composable
private fun AdminPlatformGmvCard(data: AdminPlatformAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PLATFORM GROSS MERCHANDISE VALUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
            Text(
                "₹${String.format("%,.2f", data.grossMerchandiseValue)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            HorizontalDivider(color = Navy700)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("5% Platform Commission", fontSize = 11.sp, color = TextMuted)
                    Text("₹${String.format("%,.2f", data.totalPlatformCommission)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
                Column {
                    Text("Total Orders Fulfilled", fontSize = 11.sp, color = TextMuted)
                    Text("${data.totalOrdersProcessed}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Success Rate", fontSize = 11.sp, color = TextMuted)
                    Text("${data.platformOrderSuccessRate}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberOrange)
                }
            }
        }
    }
}

@Composable
private fun AdminEcosystemHealthGrid(data: AdminPlatformAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Platform Ecosystem Health", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Active Local Shops",
                value = "${data.activeMerchants}",
                subtitle = "Onboarded merchants",
                icon = Icons.Filled.Storefront,
                iconColor = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "KYC Verified Ratio",
                value = "${data.verifiedMerchantsPercentage}%",
                subtitle = "Aadhaar / GST checked",
                icon = Icons.Filled.Verified,
                iconColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Dispute Mediation SLA",
                value = "${data.averageResolutionTimeHours} hrs",
                subtitle = "Avg turnaround",
                icon = Icons.Filled.Gavel,
                iconColor = AmberOrangeDark,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Escrow Payouts",
                value = "100%",
                subtitle = "Automated settlement",
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = BrandBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminCategoryVolumeDistribution(categories: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Category GMV Contribution", fontSize = 15.sp, fontWeight = FontWeight.Bold)

            val total = categories.values.sum().coerceAtLeast(1.0)

            categories.forEach { (cat, amount) ->
                val fraction = (amount / total).toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("₹${String.format("%,.0f", amount)} (${(fraction * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = BrandBlue,
                        trackColor = BorderLight
                    )
                }
            }
        }
    }
}

// ---------------- PROMOTIONS / COUPONS SECTION ----------------

@Composable
private fun PromotionsManagerSection(
    coupons: List<PromoCoupon>,
    onAddNew: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Promotions & Store Coupons", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAddNew) {
                    Text("+ Create Coupon", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                }
            }

            coupons.forEach { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberOrangeSoft)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(coupon.code, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = AmberOrangeDark)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Min Order ₹${coupon.minOrderAmount.toInt()} • Max Disc ₹${coupon.maxDiscountAmount.toInt()} • Exp ${coupon.expiryDate}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveNeighborhoodDeals(coupons: List<PromoCoupon>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Active Neighborhood Deals & Coupons", fontSize = 15.sp, fontWeight = FontWeight.Bold)

            coupons.forEach { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberOrangeSoft.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, AmberOrange.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Discount, contentDescription = null, tint = AmberOrangeDark)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coupon.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy900)
                            Text("Use code ${coupon.code} on checkout • Min order ₹${coupon.minOrderAmount.toInt()}", fontSize = 11.sp, color = Navy800)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Navy900)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(coupon.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CREATE COUPON DIALOG ----------------

@Composable
private fun CreateCouponDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, title: String, discount: Int, minOrder: Double, maxDisc: Double, expiry: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("15") }
    var minOrder by remember { mutableStateOf("399") }
    var maxDisc by remember { mutableStateOf("100") }
    var expiry by remember { mutableStateOf("30 Nov 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Promotional Coupon", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Coupon Code (e.g. FESTIVE20)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Offer Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it },
                        label = { Text("Min Order ₹") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = maxDisc,
                        onValueChange = { maxDisc = it },
                        label = { Text("Max Disc ₹") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("Expiry Date") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank() && title.isNotBlank()) {
                        onConfirm(
                            code,
                            title,
                            discount.toIntOrNull() ?: 10,
                            minOrder.toDoubleOrNull() ?: 299.0,
                            maxDisc.toDoubleOrNull() ?: 50.0,
                            expiry
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Publish Coupon")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
