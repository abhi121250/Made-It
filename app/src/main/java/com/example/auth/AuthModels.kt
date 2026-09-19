package com.example.auth

enum class UserRole(val label: String, val badge: String) {
    CUSTOMER("Customer", "Local Shopper"),
    PROVIDER("Business Provider", "Verified Store/Pro"),
    ADMIN("Administrator", "Super Admin")
}

data class AuthUser(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val businessName: String? = null,
    val isVerified: Boolean = false,
    val city: String = "Bengaluru",
    val authToken: String = "jwt_session_token_made_it"
)

sealed interface AuthState {
    data object Idle : AuthState
    data class OtpSent(
        val phone: String,
        val verificationId: String,
        val autoFilledOtp: String? = null,
        val timeoutSeconds: Int = 30
    ) : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
}
