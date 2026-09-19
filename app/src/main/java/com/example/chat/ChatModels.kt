package com.example.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SenderType {
    CUSTOMER,
    PROVIDER,
    ADMIN,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val senderName: String,
    val senderType: SenderType,
    val text: String,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
    val isFromMe: Boolean = false,
    val isSystemNotice: Boolean = false
)

data class ChatConversation(
    val conversationId: String,
    val participantName: String,
    val participantRole: String,
    val businessOrServiceTitle: String,
    val orderReference: String? = null,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val phoneNumber: String = "9876543210"
)
