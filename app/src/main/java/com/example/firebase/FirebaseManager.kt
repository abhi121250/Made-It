package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.auth.AuthUser
import com.example.auth.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth initialization notice: ${e.message}")
            null
        }
    }

    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore initialization notice: ${e.message}")
            null
        }
    }

    /**
     * Persists or updates the user profile in Firestore collection "users"
     */
    suspend fun saveUserToFirestore(user: AuthUser): Boolean {
        val db = firestore ?: return false
        return try {
            val userMap = hashMapOf(
                "id" to user.id,
                "fullName" to user.fullName,
                "phone" to user.phone,
                "email" to (user.email ?: ""),
                "role" to user.role.name,
                "businessName" to (user.businessName ?: ""),
                "isVerified" to user.isVerified,
                "city" to user.city,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(user.id)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d(TAG, "User persisted to Firestore successfully: ${user.id}")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firestore saveUser failed (using local cache): ${e.message}")
            false
        }
    }

    /**
     * Persists customer or provider order to Firestore collection "orders"
     */
    suspend fun saveOrderToFirestore(
        orderId: String,
        customerId: String,
        providerId: String,
        storeName: String,
        totalAmount: Double,
        status: String,
        itemsSummary: String
    ): Boolean {
        val db = firestore ?: return false
        return try {
            val orderMap = hashMapOf(
                "orderId" to orderId,
                "customerId" to customerId,
                "providerId" to providerId,
                "storeName" to storeName,
                "totalAmount" to totalAmount,
                "status" to status,
                "itemsSummary" to itemsSummary,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("orders")
                .document(orderId)
                .set(orderMap, SetOptions.merge())
                .await()
            Log.d(TAG, "Order synced to Firestore: $orderId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firestore saveOrder notice: ${e.message}")
            false
        }
    }

    /**
     * Signs in with Firebase Auth anonymously or with Google credentials
     */
    suspend fun signInWithFirebase(): FirebaseUser? {
        val authInstance = auth ?: return null
        return try {
            val current = authInstance.currentUser
            if (current != null) {
                current
            } else {
                val result = authInstance.signInAnonymously().await()
                result.user
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth sign-in notice: ${e.message}")
            null
        }
    }
}
