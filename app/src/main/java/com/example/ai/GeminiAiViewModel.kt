package com.example.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeminiAiViewModel(
    private val repository: GeminiAiRepository = GeminiAiRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                id = "init_welcome",
                isUser = false,
                text = "👋 Welcome to **Made It AI Assistant**!\n\nI can help you search live market prices with **Google Search Grounding**, locate local stores with **Google Maps Grounding**, diagnose repair issues, or help merchants optimize store growth.\n\nChoose a model or persona below to begin!",
                modelUsed = GeminiModel.GEMINI_3_5_FLASH,
                groundingMode = GroundingMode.GOOGLE_SEARCH
            )
        )
    )
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _selectedModel = MutableStateFlow(GeminiModel.GEMINI_3_5_FLASH)
    val selectedModel: StateFlow<GeminiModel> = _selectedModel.asStateFlow()

    private val _activeGrounding = MutableStateFlow(GroundingMode.GOOGLE_SEARCH)
    val activeGrounding: StateFlow<GroundingMode> = _activeGrounding.asStateFlow()

    private val _activePersona = MutableStateFlow(AssistantPersona.LOCAL_SHOPPING_CONCIERGE)
    val activePersona: StateFlow<AssistantPersona> = _activePersona.asStateFlow()

    fun setModel(model: GeminiModel) {
        _selectedModel.value = model
    }

    fun setGrounding(grounding: GroundingMode) {
        _activeGrounding.value = grounding
    }

    fun setPersona(persona: AssistantPersona) {
        _activePersona.value = persona
    }

    fun sendMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val userMessage = AiChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = trimmed,
            modelUsed = _selectedModel.value,
            groundingMode = _activeGrounding.value
        )

        val updatedHistory = _messages.value + userMessage
        _messages.value = updatedHistory
        _isGenerating.value = true

        viewModelScope.launch {
            val reply = repository.generateResponse(
                history = updatedHistory,
                newPrompt = trimmed,
                model = _selectedModel.value,
                groundingMode = _activeGrounding.value,
                persona = _activePersona.value
            )
            _messages.value = _messages.value + reply
            _isGenerating.value = false
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            AiChatMessage(
                id = "init_welcome_new",
                isUser = false,
                text = "Conversation reset. How can I assist your local shopping or service needs?",
                modelUsed = _selectedModel.value,
                groundingMode = _activeGrounding.value
            )
        )
    }
}
