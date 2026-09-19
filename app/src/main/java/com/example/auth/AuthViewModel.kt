package com.example.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val _selectedRole = MutableStateFlow(UserRole.CUSTOMER)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }

    fun sendOtp(phone: String) {
        if (phone.length < 10) {
            _uiState.value = AuthState.Error("Please enter a valid 10-digit mobile number")
            return
        }

        val formattedPhone = if (phone.startsWith("+91")) phone else "+91$phone"
        _uiState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.sendOtp(formattedPhone)
            result.onSuccess { verificationId ->
                // In development demo mode, pre-fill sample OTP 123456 for instant testing convenience
                _uiState.value = AuthState.OtpSent(
                    phone = formattedPhone,
                    verificationId = verificationId,
                    autoFilledOtp = "123456",
                    timeoutSeconds = 30
                )
            }.onFailure { error ->
                _uiState.value = AuthState.Error(error.message ?: "Failed to send OTP. Please retry.")
            }
        }
    }

    fun verifyOtp(
        phone: String,
        otp: String,
        fullName: String,
        businessName: String? = null
    ) {
        if (otp.length != 6) {
            _uiState.value = AuthState.Error("OTP must be 6 digits")
            return
        }

        _uiState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.verifyOtp(
                phone = phone,
                otp = otp,
                role = _selectedRole.value,
                fullName = fullName,
                businessName = businessName
            )
            result.onSuccess { user ->
                _uiState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthState.Error(error.message ?: "Invalid OTP entered")
            }
        }
    }

    fun signInWithPassword(phoneOrEmail: String, pass: String) {
        if (phoneOrEmail.isBlank() || pass.isBlank()) {
            _uiState.value = AuthState.Error("Please provide both identifier and password")
            return
        }

        _uiState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.loginWithPassword(phoneOrEmail, pass)
            result.onSuccess { user ->
                _uiState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthState.Error(error.message ?: "Invalid credentials")
            }
        }
    }

    fun signInWithGoogle() {
        _uiState.value = AuthState.Loading
        viewModelScope.launch {
            val result = repository.signInWithGoogle(
                role = _selectedRole.value,
                accountName = if (_selectedRole.value == UserRole.PROVIDER) "Rajesh Sharma (Services)" else "Priya Patel",
                email = if (_selectedRole.value == UserRole.PROVIDER) "rajesh.sharma@gmail.com" else "priya.patel@gmail.com"
            )
            result.onSuccess { user ->
                _uiState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthState.Error(error.message ?: "Google Sign-in was cancelled or failed")
            }
        }
    }

    fun resetToIdle() {
        _uiState.value = AuthState.Idle
    }

    fun signOut() {
        _uiState.value = AuthState.Idle
    }
}
