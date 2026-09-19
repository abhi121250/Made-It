package com.example.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val _conversations = MutableStateFlow(
        listOf(
            ChatConversation(
                conversationId = "conv_1",
                participantName = "Shree Balaji Kirana & Supermarket",
                participantRole = "Merchant",
                businessOrServiceTitle = "Order #ORD-8492 (Groceries & Atta)",
                orderReference = "#ORD-8492",
                lastMessage = "Your package is packed and will leave for delivery in 10 mins!",
                lastMessageTime = "10:45 AM",
                unreadCount = 1,
                phoneNumber = "9823456789"
            ),
            ChatConversation(
                conversationId = "conv_2",
                participantName = "Ramesh Kumar (Expert Electrician)",
                participantRole = "Service Technician",
                businessOrServiceTitle = "Booking #SRV-3910 (Switchboard Repair)",
                orderReference = "#SRV-3910",
                lastMessage = "I am nearby Sector 4. Reaching your location by 3:15 PM.",
                lastMessageTime = "09:30 AM",
                unreadCount = 0,
                phoneNumber = "9876123450"
            ),
            ChatConversation(
                conversationId = "conv_3",
                participantName = "MADE IT Support & Help Desk",
                participantRole = "Platform Support",
                businessOrServiceTitle = "Instant Chat & Dispute Help",
                orderReference = null,
                lastMessage = "Welcome to MADE IT! Let us know if you need assistance with any local orders.",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                phoneNumber = "1800123456"
            )
        )
    )
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _selectedConversationId = MutableStateFlow<String?>("conv_1")
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _messagesMap = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "conv_1" to listOf(
                ChatMessage(
                    id = "m1",
                    senderName = "System",
                    senderType = SenderType.SYSTEM,
                    text = "Order #ORD-8492 has been confirmed by Shree Balaji Kirana.",
                    timestamp = "10:30 AM",
                    isSystemNotice = true
                ),
                ChatMessage(
                    id = "m2",
                    senderName = "You",
                    senderType = SenderType.CUSTOMER,
                    text = "Hello, please ensure the Aashirvaad Atta pack is from the latest fresh manufacturing batch.",
                    timestamp = "10:32 AM",
                    isFromMe = true
                ),
                ChatMessage(
                    id = "m3",
                    senderName = "Shree Balaji Kirana",
                    senderType = SenderType.PROVIDER,
                    text = "Namaste! Yes, absolutely. We have checked and packed the brand new stock.",
                    timestamp = "10:38 AM",
                    isFromMe = false
                ),
                ChatMessage(
                    id = "m4",
                    senderName = "Shree Balaji Kirana",
                    senderType = SenderType.PROVIDER,
                    text = "Your package is packed and will leave for delivery in 10 mins!",
                    timestamp = "10:45 AM",
                    isFromMe = false
                )
            ),
            "conv_2" to listOf(
                ChatMessage(
                    id = "m10",
                    senderName = "System",
                    senderType = SenderType.SYSTEM,
                    text = "Service visit #SRV-3910 booked for today 03:00 PM.",
                    timestamp = "09:00 AM",
                    isSystemNotice = true
                ),
                ChatMessage(
                    id = "m11",
                    senderName = "Ramesh Kumar",
                    senderType = SenderType.PROVIDER,
                    text = "I am nearby Sector 4. Reaching your location by 3:15 PM.",
                    timestamp = "09:30 AM",
                    isFromMe = false
                )
            ),
            "conv_3" to listOf(
                ChatMessage(
                    id = "m20",
                    senderName = "MADE IT Support",
                    senderType = SenderType.ADMIN,
                    text = "Welcome to MADE IT! Let us know if you need assistance with any local orders.",
                    timestamp = "Yesterday",
                    isFromMe = false
                )
            )
        )
    )
    val messagesMap: StateFlow<Map<String, List<ChatMessage>>> = _messagesMap.asStateFlow()

    fun selectConversation(id: String) {
        _selectedConversationId.value = id
        // Mark conversation as read
        _conversations.update { list ->
            list.map { if (it.conversationId == id) it.copy(unreadCount = 0) else it }
        }
    }

    fun sendMessage(text: String, senderName: String, senderType: SenderType) {
        val convId = _selectedConversationId.value ?: return
        if (text.isBlank()) return

        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            senderType = senderType,
            text = text.trim(),
            timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            isFromMe = true
        )

        _messagesMap.update { current ->
            val list = current[convId] ?: emptyList()
            current + (convId to (list + newMsg))
        }

        _conversations.update { list ->
            list.map { conv ->
                if (conv.conversationId == convId) {
                    conv.copy(
                        lastMessage = text.trim(),
                        lastMessageTime = newMsg.timestamp
                    )
                } else conv
            }
        }
    }
}
