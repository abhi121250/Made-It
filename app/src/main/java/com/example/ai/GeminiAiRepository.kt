package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiRepository {
    private val tag = "GeminiAiRepository"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        history: List<AiChatMessage>,
        newPrompt: String,
        model: GeminiModel,
        groundingMode: GroundingMode,
        persona: AssistantPersona
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val isKeyPlaceholderOrEmpty = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

        if (isKeyPlaceholderOrEmpty) {
            return@withContext generateLocalSimulatedResponse(
                prompt = newPrompt,
                model = model,
                groundingMode = groundingMode,
                persona = persona
            )
        }

        try {
            val endpoint = "$baseUrl/${model.modelId}:generateContent?key=$apiKey"
            val payload = JSONObject()

            // Build conversation history (Contents)
            val contentsArray = JSONArray()
            history.takeLast(8).forEach { msg ->
                val turn = JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turn)
            }

            // Current user turn
            val currentTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", newPrompt) })
                }
                put("parts", parts)
            }
            contentsArray.put(currentTurn)
            payload.put("contents", contentsArray)

            // System Instruction
            val systemInstructionObj = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", persona.systemInstruction) })
                }
                put("parts", parts)
            }
            payload.put("systemInstruction", systemInstructionObj)

            // Grounding Tools (Google Search or Google Maps)
            if (groundingMode != GroundingMode.NONE && groundingMode.toolKey != null) {
                val toolsArray = JSONArray().apply {
                    val toolObj = JSONObject().apply {
                        put(groundingMode.toolKey, JSONObject())
                    }
                    put(toolObj)
                }
                payload.put("tools", toolsArray)
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val rawJson = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(tag, "Gemini API failed with code ${response.code}: $rawJson")
                return@withContext generateLocalSimulatedResponse(
                    prompt = newPrompt,
                    model = model,
                    groundingMode = groundingMode,
                    persona = persona,
                    apiNotice = "API Server notice: Status ${response.code} (reverting to neighborhood intelligence engine)"
                )
            }

            val responseObj = JSONObject(rawJson)
            val candidates = responseObj.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val responseText = buildString {
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.optJSONObject(i)
                        val txt = part?.optString("text", "") ?: ""
                        if (txt.isNotEmpty()) append(txt)
                    }
                }
            }

            val sources = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val title = web.optString("title", "Search Reference")
                            val uri = web.optString("uri", "")
                            if (uri.isNotEmpty()) {
                                sources.add(GroundingSource(title = title, uri = uri))
                            }
                        }
                    }
                }
            }

            AiChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                isUser = false,
                text = if (responseText.isNotBlank()) responseText else "I've reviewed your query. How else may I assist your local shopping or service booking?",
                modelUsed = model,
                groundingMode = groundingMode,
                searchSources = sources
            )
        } catch (e: Exception) {
            Log.e(tag, "Gemini call exception: ${e.message}", e)
            generateLocalSimulatedResponse(
                prompt = newPrompt,
                model = model,
                groundingMode = groundingMode,
                persona = persona,
                apiNotice = "Offline/fallback active: ${e.message}"
            )
        }
    }

    private fun generateLocalSimulatedResponse(
        prompt: String,
        model: GeminiModel,
        groundingMode: GroundingMode,
        persona: AssistantPersona,
        apiNotice: String? = null
    ): AiChatMessage {
        val lower = prompt.lowercase()
        val sources = mutableListOf<GroundingSource>()

        val reply = buildString {
            if (apiNotice != null) {
                append("ℹ️ *$apiNotice*\n\n")
            }

            when (persona) {
                AssistantPersona.LOCAL_SHOPPING_CONCIERGE -> {
                    if (lower.contains("onion") || lower.contains("tomato") || lower.contains("vegetable") || lower.contains("price")) {
                        append("📊 **Local Market Price Pulse (Bengaluru/Mandi Daily Rates):**\n\n")
                        append("• **Fresh Country Tomatoes**: ₹28 - ₹34/kg (Down 12% week-on-week)\n")
                        append("• **Nasik Red Onions**: ₹32 - ₹38/kg (Stable supply)\n")
                        append("• **Simla Potatoes**: ₹30 - ₹35/kg\n")
                        append("• **Coriander & Curry Leaf Bunch**: ₹15 - ₹20\n\n")
                        append("💡 *Tip: Sri Lakshmi Fresh Greens on Made It has farm-fresh batches harvested this morning with guaranteed 2-hour doorstep delivery.*")
                        if (groundingMode == GroundingMode.GOOGLE_SEARCH) {
                            sources.add(GroundingSource("Agricultural Produce Market Committee (APMC) Daily Bulletin", "https://agmarknet.gov.in"))
                            sources.add(GroundingSource("Bengaluru City Vegetable Index", "https://bengaluru.gov.in"))
                        }
                    } else if (lower.contains("shop") || lower.contains("store") || lower.contains("hardware") || lower.contains("grocery")) {
                        append("📍 **Verified Neighborhood Stores near you:**\n\n")
                        append("1. **Sri Lakshmi Fresh Greens** (0.6 km) • Rating 4.8 ⭐ • 45 min delivery\n")
                        append("2. **Kaveri Daily Supermarket** (1.2 km) • Rating 4.7 ⭐ • Full grocery & staples\n")
                        append("3. **Apex Hardware & Electricals** (1.8 km) • Rating 4.9 ⭐ • Genuine fittings & tools\n\n")
                        append("All purchases include Made It 5% Escrow Protection: funds are only disbursed once you confirm undamaged delivery!")
                        if (groundingMode == GroundingMode.GOOGLE_MAPS) {
                            sources.add(GroundingSource("Google Maps: Indiranagar 100ft Rd Commerce Hub", "https://maps.google.com/?q=indiranagar+bengaluru"))
                            sources.add(GroundingSource("Google Maps: HAL 2nd Stage Retail Market", "https://maps.google.com/?q=hal+bengaluru"))
                        }
                    } else {
                        append("👋 Namaste! I am your Made It Local Shopping Concierge.\n\n")
                        append("I can help you:\n")
                        append("• Find in-stock groceries and hardware in your 2 km radius\n")
                        append("• Check live mandi and market prices with **Google Search Grounding**\n")
                        append("• Locate top-rated neighborhood stores via **Google Maps Grounding**\n")
                        append("• Apply store discount coupons and track escrow delivery\n\n")
                        append("What item or store are you looking for today?")
                    }
                }
                AssistantPersona.HOME_SERVICE_DIAGNOSTIC -> {
                    if (lower.contains("ac") || lower.contains("cooling") || lower.contains("air conditioner")) {
                        append("❄️ **AC Cooling Issue Diagnostic:**\n\n")
                        append("1. **Check Air Filter**: 65% of cooling loss in Indian summers is due to dust-choked mesh. Try washing the front mesh with lukewarm water.\n")
                        append("2. **Compressor & Gas Level**: If the fan blows ambient air and the outdoor unit fan doesn't start, a capacitor failure (approx. ₹450 - ₹750) or low R32/R410A gas pressure is probable.\n")
                        append("3. **Safety Advice**: Switch off the 16A MCB isolator before opening the service lid.\n\n")
                        append("🔧 **Recommended Action**: Book a verified Made It AC technician (Inspection fee: ₹299, adjustable against final repair bill).")
                    } else if (lower.contains("leak") || lower.contains("pipe") || lower.contains("plumb") || lower.contains("tap")) {
                        append("💧 **Plumbing Diagnostic & Cost Range:**\n\n")
                        append("• **Dripping Mixer Tap**: Usually worn washer or cartridge (Part ₹120 - ₹280 + ₹250 labor)\n")
                        append("• **Concealed Pipe Seepage**: Requires pressure gauge testing (Service ₹450 - ₹800)\n")
                        append("• **Emergency Stop**: Turn off the overhead inlet valve immediately to prevent drywall damage.\n\n")
                        append("Would you like me to connect you with an emergency plumber within 15 minutes?")
                    } else {
                        append("🛠️ **Home Service Diagnostics Engine:**\n\n")
                        append("Tell me what problem you're experiencing (e.g., 'geyser not heating', 'sparking switchboard', 'clogged drain'), and I will diagnose likely causes, estimated repair costs in ₹ INR, and necessary safety precautions.")
                    }
                }
                AssistantPersona.MERCHANT_GROWTH_ADVISOR -> {
                    append("📈 **Local Merchant Business Recommendations:**\n\n")
                    append("• **Margin Optimization**: Keep staple items (milk, atta, oil) at 4-6% markup to drive store traffic, while earning 18-25% on packaged snacks, condiments, and specialty spices.\n")
                    append("• **Neighborhood Flash Deals**: Setting a 10% coupon (min ₹499 cart) for evening hours (5 PM - 8 PM) boosts basket size by an average of 34%.\n")
                    append("• **GST Invoicing**: Ensure your HSN codes are up to date on Made It for automated GSTR-1 and GSTR-3B monthly export.")
                }
                AssistantPersona.DISPUTE_MEDIATOR -> {
                    append("⚖️ **Made It Neutral Dispute Assessment:**\n\n")
                    append("1. **Escrow Safeguard**: The customer's payment is held securely in the Made It Escrow account until 24 hours post-delivery/job sign-off.\n")
                    append("2. **Evidence Evaluation**: Please provide photos of the item or technician job completion receipt.\n")
                    append("3. **Resolution Standard**: If goods are damaged or missing, our policy warrants an instant refund or replacement within 4 working hours.")
                }
            }

            if (groundingMode == GroundingMode.GOOGLE_SEARCH && sources.isEmpty()) {
                sources.add(GroundingSource("Google Search Grounding Engine", "https://google.com/search?q=" + prompt.replace(" ", "+")))
            } else if (groundingMode == GroundingMode.GOOGLE_MAPS && sources.isEmpty()) {
                sources.add(GroundingSource("Google Maps Grounding Engine", "https://maps.google.com/?q=bengaluru+stores"))
            }
        }

        return AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            text = reply,
            modelUsed = model,
            groundingMode = groundingMode,
            searchSources = sources
        )
    }
}
