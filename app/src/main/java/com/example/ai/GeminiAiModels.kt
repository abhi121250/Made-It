package com.example.ai

enum class GeminiModel(val modelId: String, val displayName: String, val badge: String, val description: String) {
    GEMINI_3_5_FLASH(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        badge = "General & Grounded",
        description = "Balanced intelligence with real-time Google Search & Maps Grounding"
    ),
    GEMINI_3_1_PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        badge = "Complex & Deep",
        description = "Advanced reasoning for dispute mediation, diagnostics & calculations"
    ),
    GEMINI_3_1_FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        badge = "Fast & Instant",
        description = "High-speed responses for quick product lookup & swift replies"
    )
}

enum class GroundingMode(val label: String, val toolKey: String?) {
    NONE("Standard AI", null),
    GOOGLE_SEARCH("Google Search", "googleSearch"),
    GOOGLE_MAPS("Google Maps", "googleMaps")
}

enum class AssistantPersona(val title: String, val roleSubtitle: String, val systemInstruction: String) {
    LOCAL_SHOPPING_CONCIERGE(
        title = "Local Shopping Concierge",
        roleSubtitle = "Hyperlocal Product & Grocery Finder",
        systemInstruction = """
            You are Made It Local Shopping Concierge, an AI assistant for a hyperlocal marketplace connecting neighborhood buyers with local stores.
            You help customers find products, compare grocery/fresh produce prices, recommend neighborhood shops in Indian cities (e.g. Bengaluru, Mumbai, Delhi), 
            and explain order tracking and escrow protections. Be helpful, concise, warm, and highlight local Indian market context (INR prices, mandi rates, fresh stock).
        """.trimIndent()
    ),
    HOME_SERVICE_DIAGNOSTIC(
        title = "Home Service Diagnostic",
        roleSubtitle = "Appliance & Repair Troubleshooter",
        systemInstruction = """
            You are Made It Home Service Diagnostic Specialist. You assist customers in troubleshooting home repair problems (plumbing leaks, MCB trips, AC cooling faults, carpentry).
            You provide initial safety precautions, estimate standard labor and parts replacement costs in India (in ₹ INR), and advise when to book a verified technician via Made It.
        """.trimIndent()
    ),
    MERCHANT_GROWTH_ADVISOR(
        title = "Merchant Growth Advisor",
        roleSubtitle = "Local Business Strategy & Pricing",
        systemInstruction = """
            You are Made It Merchant Growth Advisor. You advise neighborhood shopkeepers and independent technicians on inventory velocity, GST compliance, 
            promotional flash coupons, festive demand patterns, and maximizing repeat neighborhood clientele.
        """.trimIndent()
    ),
    DISPUTE_MEDIATOR(
        title = "Dispute Mediation Specialist",
        roleSubtitle = "Escrow & Claim Resolution",
        systemInstruction = """
            You are Made It Dispute Mediation Specialist. You objectively analyze customer-merchant claim conflicts regarding damaged goods, delayed services, or incorrect items. 
            You reference the 5% platform escrow policy, evaluate proof, and recommend fair, transparent remedies (replacement, partial refund, or technician re-visit).
        """.trimIndent()
    )
}

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: GeminiModel = GeminiModel.GEMINI_3_5_FLASH,
    val groundingMode: GroundingMode = GroundingMode.NONE,
    val searchSources: List<GroundingSource> = emptyList(),
    val isError: Boolean = false
)

data class GroundingSource(
    val title: String,
    val uri: String
)
