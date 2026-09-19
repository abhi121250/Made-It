package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.chat.ChatMessage
import com.example.chat.ChatViewModel
import com.example.chat.SenderType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    user: AuthUser,
    chatViewModel: ChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by chatViewModel.conversations.collectAsState()
    val selectedConvId by chatViewModel.selectedConversationId.collectAsState()
    val messagesMap by chatViewModel.messagesMap.collectAsState()

    val currentConversation = conversations.find { it.conversationId == selectedConvId } ?: conversations.firstOrNull()
    val messages = messagesMap[currentConversation?.conversationId] ?: emptyList()

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (user.role == UserRole.PROVIDER) AmberOrangeSoft else BrandBlueSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (user.role) {
                                    UserRole.PROVIDER -> Icons.Filled.Person
                                    UserRole.CUSTOMER -> Icons.Filled.Storefront
                                    UserRole.ADMIN -> Icons.Filled.SupportAgent
                                },
                                contentDescription = null,
                                tint = if (user.role == UserRole.PROVIDER) AmberOrangeDark else BrandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = currentConversation?.participantName ?: "Made It Direct Chat",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = currentConversation?.participantRole ?: "Active Channel",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Simulated direct phone dial */ }) {
                        Icon(Icons.Filled.Call, contentDescription = "Call", tint = EmeraldGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Conversation Switcher Bar
            ScrollableTabRow(
                selectedTabIndex = conversations.indexOfFirst { it.conversationId == selectedConvId }.coerceAtLeast(0),
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { HorizontalDivider(color = BorderLight) }
            ) {
                conversations.forEach { conv ->
                    val isSelected = conv.conversationId == selectedConvId
                    Tab(
                        selected = isSelected,
                        onClick = { chatViewModel.selectConversation(conv.conversationId) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    conv.participantName.take(18) + (if (conv.participantName.length > 18) "…" else ""),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (conv.unreadCount > 0) {
                                    Badge(containerColor = AmberOrange, contentColor = Navy900) {
                                        Text("${conv.unreadCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Order / Booking Context Banner
            currentConversation?.orderReference?.let { orderRef ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandBlueSoft),
                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Linked Order Reference: $orderRef", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                            Text(currentConversation.businessOrServiceTitle, fontSize = 11.sp, color = Navy800)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { msg ->
                    if (msg.isSystemNotice) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BorderLight)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        val isMe = msg.isFromMe
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isMe) 14.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 14.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) BrandBlue else MaterialTheme.colorScheme.surface
                                ),
                                border = if (!isMe) BorderStroke(1.dp, BorderLight) else null,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    if (!isMe) {
                                        Text(
                                            text = msg.senderName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberOrangeDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 9.sp,
                                        color = if (isMe) Color.White.copy(alpha = 0.7f) else TextMuted,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick reply suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = when (user.role) {
                    UserRole.PROVIDER -> listOf("Packed & ready", "Out for delivery", "Reaching in 10 mins")
                    UserRole.CUSTOMER -> listOf("Is this in fresh stock?", "Please deliver by 6 PM", "Call before arrival")
                    UserRole.ADMIN -> listOf("Escrow verified", "Mediation in progress", "Refund credited")
                }
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = {
                            val senderType = when (user.role) {
                                UserRole.CUSTOMER -> SenderType.CUSTOMER
                                UserRole.PROVIDER -> SenderType.PROVIDER
                                UserRole.ADMIN -> SenderType.ADMIN
                            }
                            chatViewModel.sendMessage(suggestion, user.fullName, senderType)
                        },
                        label = { Text(suggestion, fontSize = 11.sp) }
                    )
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Type message or inquiry…", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                val senderType = when (user.role) {
                                    UserRole.CUSTOMER -> SenderType.CUSTOMER
                                    UserRole.PROVIDER -> SenderType.PROVIDER
                                    UserRole.ADMIN -> SenderType.ADMIN
                                }
                                chatViewModel.sendMessage(inputMessage, user.fullName, senderType)
                                inputMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandBlue)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
