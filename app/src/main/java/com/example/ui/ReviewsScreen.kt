package com.example.ui

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
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.example.reviews.DisputeStatus
import com.example.reviews.MadeItDispute
import com.example.reviews.MadeItReview
import com.example.reviews.ReviewsViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    user: AuthUser,
    reviewsViewModel: ReviewsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviews by reviewsViewModel.reviews.collectAsState()
    val disputes by reviewsViewModel.disputes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Reviews & Ratings, 1: Help & Disputes
    var showWriteReviewDialog by remember { mutableStateOf(false) }
    var showRaiseDisputeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Reviews & Feedback" else "Disputes & Mediation",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MADE IT Trust & Verification Center",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reviews_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 && user.role == UserRole.CUSTOMER) {
                FloatingActionButton(
                    onClick = { showWriteReviewDialog = true },
                    containerColor = AmberOrange,
                    contentColor = Navy900,
                    modifier = Modifier.testTag("write_review_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.RateReview, contentDescription = "Write Review")
                        Text("Rate Store", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (selectedTab == 1 && user.role == UserRole.CUSTOMER) {
                FloatingActionButton(
                    onClick = { showRaiseDisputeDialog = true },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("raise_dispute_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ReportProblem, contentDescription = "Raise Issue")
                        Text("Report Issue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Store Reviews (${reviews.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Reviews") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Disputes (${disputes.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.Gavel, contentDescription = "Disputes") }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (selectedTab == 0) {
                    // Reviews Summary Banner
                    item {
                        ReviewsSummaryCard(reviews)
                    }

                    items(reviews) { review ->
                        ReviewCard(
                            review = review,
                            canReply = user.role == UserRole.PROVIDER,
                            onReply = { replyText ->
                                reviewsViewModel.addProviderReply(review.id, replyText)
                            }
                        )
                    }
                } else {
                    // Disputes Banner
                    item {
                        DisputeHeaderCard()
                    }

                    items(disputes) { dispute ->
                        DisputeCard(
                            dispute = dispute,
                            isAdmin = user.role == UserRole.ADMIN,
                            onResolve = { notes ->
                                reviewsViewModel.resolveDispute(dispute.id, notes)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showWriteReviewDialog) {
        WriteReviewDialog(
            onDismiss = { showWriteReviewDialog = false },
            onSubmit = { rating, comment ->
                reviewsViewModel.addReview(
                    storeId = "store_kumar_solutions",
                    storeName = "Kumar Home & Hardware Hub",
                    customerName = user.fullName ?: user.phone,
                    rating = rating,
                    comment = comment
                )
                showWriteReviewDialog = false
            }
        )
    }

    if (showRaiseDisputeDialog) {
        RaiseDisputeDialog(
            onDismiss = { showRaiseDisputeDialog = false },
            onSubmit = { orderId, reason, description, refund ->
                reviewsViewModel.raiseDispute(
                    orderId = orderId,
                    storeName = "Kumar Home & Hardware Hub",
                    reason = reason,
                    description = description,
                    refundAmount = refund
                )
                showRaiseDisputeDialog = false
            }
        )
    }
}

@Composable
private fun ReviewsSummaryCard(reviews: List<MadeItReview>) {
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 5.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Overall Store Rating", color = TextMuted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = String.format("%.1f", avgRating),
                        color = AmberOrange,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Column {
                        Row {
                            repeat(5) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(text = "${reviews.size} Verified Purchases", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = "Trust", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    Text("100% Genuine", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: MadeItReview,
    canReply: Boolean,
    onReply: (String) -> Unit
) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrandBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.customerName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )
                    }
                    Column {
                        Text(text = review.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${review.storeName} • ${review.createdAt}", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = AmberOrange, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Text(text = review.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

            if (review.providerReply != null) {
                Surface(
                    color = BrandBlueSoft,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Store, contentDescription = "Store", tint = BrandBlue, modifier = Modifier.size(14.dp))
                            Text("Store Owner Response", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrandBlue)
                        }
                        Text(text = review.providerReply, fontSize = 12.sp, color = Navy900)
                    }
                }
            } else if (canReply && !showReplyField) {
                TextButton(onClick = { showReplyField = true }) {
                    Icon(Icons.Filled.Reply, contentDescription = "Reply", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reply to customer review", fontSize = 12.sp)
                }
            }

            if (showReplyField) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        placeholder = { Text("Write response...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (replyInput.isNotBlank()) {
                                onReply(replyInput)
                                showReplyField = false
                            }
                        }
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
private fun DisputeHeaderCard() {
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
            Icon(Icons.Filled.Shield, contentDescription = "Guarantee", tint = BrandBlue, modifier = Modifier.size(32.dp))
            Column {
                Text("SB Creatives Buyer Protection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "All orders are backed by escrow moderation. We ensure prompt resolution for damaged items or service disputes within 24 hours.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DisputeCard(
    dispute: MadeItDispute,
    isAdmin: Boolean,
    onResolve: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = dispute.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Order: ${dispute.orderId} • ${dispute.storeName}", fontSize = 11.sp, color = TextSecondary)
                }

                Surface(
                    color = when (dispute.status) {
                        DisputeStatus.RESOLVED -> EmeraldGreenSoft
                        DisputeStatus.MEDIATING -> AmberOrangeSoft
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = dispute.status.label,
                        color = when (dispute.status) {
                            DisputeStatus.RESOLVED -> EmeraldGreen
                            DisputeStatus.MEDIATING -> AmberOrangeDark
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(text = "Reason: ${dispute.reason}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = dispute.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

            if (dispute.requestedRefundAmount != null) {
                Text(
                    text = "Claimed Refund: ₹${dispute.requestedRefundAmount.toInt()}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (dispute.adminResolutionNotes != null) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Moderator Resolution Note", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldGreen)
                        Text(dispute.adminResolutionNotes, fontSize = 11.sp, color = Navy900)
                    }
                }
            } else if (isAdmin && dispute.status != DisputeStatus.RESOLVED) {
                Button(
                    onClick = { onResolve("Store owner agreed to full item replacement/refund.") },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Resolve & Close Dispute", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Store & Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Select your star rating:", fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) AmberOrange else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Your detailed feedback") },
                    placeholder = { Text("How was the service, delivery speed, and pricing?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (comment.isNotBlank()) onSubmit(rating, comment) },
                enabled = comment.isNotBlank()
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RaiseDisputeDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Double?) -> Unit
) {
    var orderId by remember { mutableStateOf("ORD-89F12A01") }
    var reason by remember { mutableStateOf("Item defective or not as described") }
    var description by remember { mutableStateOf("") }
    var refundAmount by remember { mutableStateOf("249") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Issue & Request Refund", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = orderId,
                    onValueChange = { orderId = it },
                    label = { Text("Order ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Dispute") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details of the issue") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = refundAmount,
                    onValueChange = { refundAmount = it },
                    label = { Text("Claim Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        onSubmit(orderId, reason, description, refundAmount.toDoubleOrNull())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("File Dispute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
