package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.analytics.AnalyticsViewModel
import com.example.ai.GeminiAiChatScreen
import com.example.ai.GeminiAiViewModel
import com.example.auth.AuthState
import com.example.auth.AuthUser
import com.example.auth.AuthViewModel
import com.example.auth.UserRole
import com.example.catalog.CatalogViewModel
import com.example.chat.ChatViewModel
import com.example.notifications.NotificationsViewModel
import com.example.orders.OrdersViewModel
import com.example.payments.PaymentsViewModel
import com.example.profile.ProfileViewModel
import com.example.reviews.ReviewsViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = viewModel(),
    catalogViewModel: CatalogViewModel = viewModel(),
    ordersViewModel: OrdersViewModel = viewModel(),
    paymentsViewModel: PaymentsViewModel = viewModel(),
    reviewsViewModel: ReviewsViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    geminiAiViewModel: GeminiAiViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by ThemeManager.themeMode.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val notificationsList by notificationsViewModel.notifications.collectAsState()
    val unreadNotifsCount = notificationsList.count { !it.isRead }
    val chatConversations by chatViewModel.conversations.collectAsState()
    val unreadChatCount = chatConversations.sumOf { it.unreadCount }

    var phoneInput by remember { mutableStateOf("9876543210") }
    var otpInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var businessNameInput by remember { mutableStateOf("") }

    // Navigation state in authenticated mode
    var currentSubView by remember { mutableStateOf("dashboard") }

    // Email/Password login tab
    var usePasswordLogin by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("admin@madeit.sbcreatives.in") }
    var passwordInput by remember { mutableStateOf("AdminMadeIt2026!") }

    val activeState = uiState
    if (activeState is AuthState.Authenticated) {
        if (currentSubView == "profile") {
            ProfileScreen(
                user = activeState.user,
                profileViewModel = profileViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "catalog") {
            CatalogScreen(
                user = activeState.user,
                catalogViewModel = catalogViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "orders") {
            OrdersScreen(
                user = activeState.user,
                ordersViewModel = ordersViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "payments") {
            PaymentsScreen(
                user = activeState.user,
                paymentsViewModel = paymentsViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "reviews") {
            ReviewsScreen(
                user = activeState.user,
                reviewsViewModel = reviewsViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "notifications") {
            NotificationsScreen(
                notificationsViewModel = notificationsViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "analytics") {
            AnalyticsScreen(
                user = activeState.user,
                analyticsViewModel = analyticsViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "chat") {
            ChatScreen(
                user = activeState.user,
                chatViewModel = chatViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        } else if (currentSubView == "ai_chat") {
            GeminiAiChatScreen(
                user = activeState.user,
                viewModel = geminiAiViewModel,
                onBack = { currentSubView = "dashboard" }
            )
            return
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_madeit_logo),
                            contentDescription = "Made It Logo",
                            modifier = Modifier
                                .height(32.dp)
                                .width(128.dp)
                        )
                    }
                },
                actions = {
                    // Gemini AI Concierge Assistant
                    IconButton(
                        onClick = { currentSubView = "ai_chat" },
                        modifier = Modifier.testTag("top_bar_gemini_ai_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BrandBlue,
                                    contentColor = Color.White
                                ) {
                                    Text("AI", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Gemini AI Concierge",
                                tint = BrandBlue
                            )
                        }
                    }

                    // Light / Dark Theme Mode Toggle
                    IconButton(
                        onClick = { ThemeManager.toggleNextTheme() },
                        modifier = Modifier.testTag("top_bar_theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                                ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                            },
                            contentDescription = "Theme: ${themeMode.name}",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { currentSubView = "chat" },
                        modifier = Modifier.testTag("top_bar_chat_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadChatCount > 0) {
                                    Badge(
                                        containerColor = EmeraldGreen,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadChatCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = "Direct Chat",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = { currentSubView = "notifications" },
                        modifier = Modifier.testTag("top_bar_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = AmberOrange,
                                        contentColor = Navy900
                                    ) {
                                        Text("$unreadNotifsCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    AssistChip(
                        onClick = {
                            val nextRole = when (selectedRole) {
                                UserRole.CUSTOMER -> UserRole.PROVIDER
                                UserRole.PROVIDER -> UserRole.ADMIN
                                UserRole.ADMIN -> UserRole.CUSTOMER
                            }
                            viewModel.selectRole(nextRole)
                        },
                        label = { Text(selectedRole.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (selectedRole) {
                                    UserRole.CUSTOMER -> Icons.Filled.Person
                                    UserRole.PROVIDER -> Icons.Filled.Business
                                    UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                },
                                contentDescription = "Role Indicator",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(end = 8.dp).testTag("role_switch_chip")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is AuthState.Authenticated -> {
                    AuthenticatedDashboard(
                        user = state.user,
                        onSignOut = { viewModel.signOut() },
                        onNavigateToProfile = { currentSubView = "profile" },
                        onNavigateToCatalog = { currentSubView = "catalog" },
                        onNavigateToOrders = { currentSubView = "orders" },
                        onNavigateToPayments = { currentSubView = "payments" },
                        onNavigateToReviews = { currentSubView = "reviews" },
                        onNavigateToNotifications = { currentSubView = "notifications" },
                        onNavigateToAnalytics = { currentSubView = "analytics" },
                        onNavigateToChat = { currentSubView = "chat" },
                        onNavigateToAiChat = { currentSubView = "ai_chat" }
                    )
                }

                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Verifying with MADE IT secure network...",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Brand Hero Banner
                        HeroBannerCard(selectedRole)

                        // Role Selector Tabs
                        RoleSelectorSection(
                            currentRole = selectedRole,
                            onRoleSelected = { viewModel.selectRole(it) }
                        )

                        // Error Banner
                        if (state is AuthState.Error) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = state.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // OTP Step 2: Verification Input
                        if (state is AuthState.OtpSent) {
                            OtpVerificationCard(
                                phone = state.phone,
                                otp = otpInput,
                                onOtpChanged = { otpInput = it },
                                autoFilled = state.autoFilledOtp,
                                onVerify = {
                                    viewModel.verifyOtp(
                                        phone = state.phone,
                                        otp = otpInput,
                                        fullName = nameInput,
                                        businessName = businessNameInput
                                    )
                                },
                                onCancel = { viewModel.resetToIdle() }
                            )
                        } else if (usePasswordLogin) {
                            // Password Login Form
                            PasswordLoginForm(
                                email = emailInput,
                                onEmailChanged = { emailInput = it },
                                password = passwordInput,
                                onPasswordChanged = { passwordInput = it },
                                onLogin = {
                                    viewModel.signInWithPassword(emailInput, passwordInput)
                                },
                                onSwitchToOtp = { usePasswordLogin = false }
                            )
                        } else {
                            // Primary OTP Request Form
                            PhoneLoginForm(
                                role = selectedRole,
                                phone = phoneInput,
                                onPhoneChanged = { phoneInput = it },
                                name = nameInput,
                                onNameChanged = { nameInput = it },
                                businessName = businessNameInput,
                                onBusinessNameChanged = { businessNameInput = it },
                                onSendOtp = { viewModel.sendOtp(phoneInput) },
                                onGoogleSignIn = { viewModel.signInWithGoogle() },
                                onSwitchToPassword = { usePasswordLogin = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Footer
                        Text(
                            text = "MADE IT • Official Platform of SB CREATIVES\nBengaluru • Bidar • Kalaburagi • Hyderabad • Pune",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBannerCard(role: UserRole) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy900)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = AmberOrange,
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Text(
                        text = "INDIA'S LOCAL COMMERCE PLATFORM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = AmberOrange
                    )
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_madeit_logo),
                        contentDescription = "Made It Logo",
                        modifier = Modifier
                            .height(20.dp)
                            .width(80.dp)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = if (role == UserRole.PROVIDER) "Grow Your Local Business Online" else "Discover & Book Trusted Local Services",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 26.sp
            )
            Text(
                text = if (role == UserRole.PROVIDER) {
                    "Register your shop, receive quotations, manage orders, and connect with customers in your locality."
                } else {
                    "Plumbers, electricians, mechanics, local groceries, tailors and shops near you in minutes."
                },
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RoleSelectorSection(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Account Type",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Customer Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onRoleSelected(UserRole.CUSTOMER) }
                    .testTag("role_customer_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (currentRole == UserRole.CUSTOMER) 2.dp else 1.dp,
                    color = if (currentRole == UserRole.CUSTOMER) BrandBlue else BorderLight
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentRole == UserRole.CUSTOMER) BrandBlueSoft.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = "Customer",
                        tint = if (currentRole == UserRole.CUSTOMER) BrandBlue else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Customer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (currentRole == UserRole.CUSTOMER) BrandBlue else TextPrimary
                    )
                    Text(
                        text = "Buy & Book",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Provider Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onRoleSelected(UserRole.PROVIDER) }
                    .testTag("role_provider_card"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = if (currentRole == UserRole.PROVIDER) 2.dp else 1.dp,
                    color = if (currentRole == UserRole.PROVIDER) AmberOrangeDark else BorderLight
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentRole == UserRole.PROVIDER) AmberOrangeSoft.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = "Provider",
                        tint = if (currentRole == UserRole.PROVIDER) AmberOrangeDark else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Provider",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (currentRole == UserRole.PROVIDER) AmberOrangeDark else TextPrimary
                    )
                    Text(
                        text = "Shops & Pros",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneLoginForm(
    role: UserRole,
    phone: String,
    onPhoneChanged: (String) -> Unit,
    name: String,
    onNameChanged: (String) -> Unit,
    businessName: String,
    onBusinessNameChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onSwitchToPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Fast Mobile Login",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("Your Full Name") },
                placeholder = { Text("e.g. Ramesh Kumar") },
                leadingIcon = {
                    Icon(Icons.Outlined.Person, contentDescription = "Name")
                },
                modifier = Modifier.fillMaxWidth().testTag("input_full_name"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Business Name (Provider only)
            if (role == UserRole.PROVIDER) {
                OutlinedTextField(
                    value = businessName,
                    onValueChange = onBusinessNameChanged,
                    label = { Text("Business / Store Name") },
                    placeholder = { Text("e.g. Kumar Plumbing Solutions") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Store, contentDescription = "Store")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_business_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Mobile Number with +91 Prefix
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) onPhoneChanged(it) },
                label = { Text("Mobile Number") },
                placeholder = { Text("10-digit number") },
                leadingIcon = {
                    Text(
                        text = "🇮🇳 +91",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().testTag("input_phone"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = onSendOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_otp_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = "Send OTP", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get 6-Digit OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
                Text(
                    text = "  OR CONTINUE WITH  ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
            }

            // Google Sign In Button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_signin_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Google",
                    tint = BrandBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign in with Google",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onSwitchToPassword,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("switch_password_button")
            ) {
                Text(
                    text = "Use Email & Password (Admin / Provider)",
                    fontSize = 12.sp,
                    color = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun OtpVerificationCard(
    phone: String,
    otp: String,
    onOtpChanged: (String) -> Unit,
    autoFilled: String?,
    onVerify: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BrandBlue)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Message,
                        contentDescription = "SMS",
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Verify Mobile Number",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "OTP sent via SMS to $phone",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) onOtpChanged(it) },
                label = { Text("Enter 6-Digit OTP") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("input_otp"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            if (autoFilled != null) {
                AssistChip(
                    onClick = { onOtpChanged(autoFilled) },
                    label = { Text("Auto-fill Test Code: $autoFilled", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Filled.Check, contentDescription = "Auto Fill", modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("autofill_otp_chip")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Change Number")
                }
                Button(
                    onClick = onVerify,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("verify_otp_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Verify & Enter", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PasswordLoginForm(
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onSwitchToOtp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Admin & Provider Credentials",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                label = { Text("Email or Phone") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth().testTag("input_email_password"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("input_password"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("password_login_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign In with Password", fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onSwitchToOtp,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("switch_otp_button")
            ) {
                Text("Back to Mobile OTP Login", fontSize = 12.sp, color = BrandBlue)
            }
        }
    }
}

@Composable
private fun AuthenticatedDashboard(
    user: AuthUser,
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAiChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToProfile() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                if (user.role == UserRole.PROVIDER) AmberOrangeSoft else BrandBlueSoft
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (user.role) {
                                UserRole.PROVIDER -> Icons.Filled.Storefront
                                UserRole.ADMIN -> Icons.Filled.AdminPanelSettings
                                UserRole.CUSTOMER -> Icons.Filled.Person
                            },
                            contentDescription = "User Avatar",
                            tint = if (user.role == UserRole.PROVIDER) AmberOrangeDark else BrandBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user.phone,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Profile",
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(color = BorderLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = onNavigateToProfile,
                        label = { Text(user.role.label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (user.isVerified) Icons.Filled.Verified else Icons.Outlined.Pending,
                            contentDescription = "Status",
                            tint = if (user.isVerified) EmeraldGreen else AmberOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (user.isVerified) "Verified" else "Verification Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user.isVerified) EmeraldGreen else AmberOrangeDark
                        )
                    }
                }

                if (user.businessName != null) {
                    Text(
                        text = "Store/Service: ${user.businessName}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Cards depending on Role
        Text(
            text = "Platform Quick Hub",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Gemini AI Assistant Tile with Google Search & Maps Grounding
        ActionTile(
            title = "Made It AI Concierge (Gemini 3.5 & Pro)",
            subtitle = "Live mandi search, Google Maps grounding & repair diagnostics",
            icon = Icons.Filled.AutoAwesome,
            iconTint = BrandBlue,
            onClick = onNavigateToAiChat
        )

        if (user.role == UserRole.CUSTOMER) {
            ActionTile("Store & Service Direct Chat", "Live inquiry with shopkeeper, technician & support", Icons.Filled.Chat, EmeraldGreen, onNavigateToChat)
            ActionTile("Local Spend & Impact Analytics", "Spending tracker, CO2 savings & local loyalty points", Icons.Filled.Insights, BrandBlue, onNavigateToAnalytics)
            ActionTile("Notifications & Alerts", "Real-time updates on orders, visits & deals", Icons.Filled.Notifications, BrandBlue, onNavigateToNotifications)
            ActionTile("My Bookings & Orders", "Track real-time delivery and service visit status", Icons.Filled.ReceiptLong, AmberOrangeDark, onNavigateToOrders)
            ActionTile("Store Reviews & Disputes", "Rate experiences, write feedback or report order issues", Icons.Filled.Star, AmberOrangeDark, onNavigateToReviews)
            ActionTile("Payments & Receipts", "View UPI, card transactions and order payment receipts", Icons.Filled.Payments, EmeraldGreen, onNavigateToPayments)
            ActionTile("Local Store & Service Catalog", "Browse products, plumbers, electricians & groceries", Icons.Filled.Storefront, BrandBlue, onNavigateToCatalog)
            ActionTile("Manage Addresses & Profile", "Set home, office, and delivery addresses", Icons.Filled.LocationOn, EmeraldGreen, onNavigateToProfile)
            ActionTile("Saved Favorites", "Quick access to your regular local shops", Icons.Filled.Favorite, Color(0xFFEF4444), onNavigateToCatalog)
        } else if (user.role == UserRole.PROVIDER) {
            ActionTile("Store Analytics & Growth Hub", "Gross sales, order conversion, top items & GST report", Icons.Filled.Insights, BrandBlue, onNavigateToAnalytics)
            ActionTile("Customer In-App Messages & Queries", "Real-time inquiries, arrival updates & instant chat", Icons.Filled.Chat, EmeraldGreen, onNavigateToChat)
            ActionTile("Real-Time Alerts & Requests", "Incoming customer bookings and platform notifications", Icons.Filled.Notifications, BrandBlue, onNavigateToNotifications)
            ActionTile("Settlement & Instant Payouts", "Track earnings, commission deductions and bank payouts", Icons.Filled.AccountBalanceWallet, EmeraldGreen, onNavigateToPayments)
            ActionTile("Customer Reviews & Ratings", "Respond to customer feedback and manage shop reputation", Icons.Filled.Star, AmberOrangeDark, onNavigateToReviews)
            ActionTile("Incoming Orders & Service Bookings", "Accept, decline or complete customer requests", Icons.Filled.ReceiptLong, AmberOrangeDark, onNavigateToOrders)
            ActionTile("Store Catalog & Products", "Manage products, services, prices & stock availability", Icons.Filled.Inventory2, BrandBlue, onNavigateToCatalog)
            ActionTile("Business KYC & Verification", "Submit Aadhaar, GST or trade license", Icons.Filled.Badge, EmeraldGreen, onNavigateToProfile)
            ActionTile("Payout Bank & UPI Account", "Configure account for customer order settlements", Icons.Filled.AccountBalance, BrandBlue, onNavigateToProfile)
        } else {
            ActionTile("Platform BI & GMV Analytics", "Platform-wide volume, 5% escrow commission & SLA health", Icons.Filled.Insights, BrandBlue, onNavigateToAnalytics)
            ActionTile("Platform Direct Communication", "Mediate live chats with buyers, merchants and tech team", Icons.Filled.Chat, EmeraldGreen, onNavigateToChat)
            ActionTile("Platform Broadcast & Alerts", "View system alerts, order notifications and dispute pings", Icons.Filled.Notifications, BrandBlue, onNavigateToNotifications)
            ActionTile("Platform Order Management", "Review platform wide orders and dispute resolutions", Icons.Filled.ReceiptLong, AmberOrangeDark, onNavigateToOrders)
            ActionTile("Disputes & Mediation Queue", "Mediate open customer claims and issue refund resolutions", Icons.Filled.Gavel, AmberOrangeDark, onNavigateToReviews)
            ActionTile("Settlements & Escrow Oversight", "Review 5% platform commission fees and vendor payouts", Icons.Filled.AccountBalanceWallet, EmeraldGreen, onNavigateToPayments)
            ActionTile("Platform Store Inventory", "Manage stores, catalog products and categories", Icons.Filled.Storefront, BrandBlue, onNavigateToCatalog)
            ActionTile("Platform Ecosystem", "Manage stores, providers, categories, orders", Icons.Filled.Dashboard, EmeraldGreen, onNavigateToProfile)
            ActionTile("Verification Queue", "Review submitted provider KYC documents", Icons.Filled.VerifiedUser, BrandBlue, onNavigateToProfile)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sign Out
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sign_out_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = "Sign Out", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out from Made It", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = "Open", tint = TextMuted)
        }
    }
}
