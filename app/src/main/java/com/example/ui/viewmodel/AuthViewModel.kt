package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionPrefs
import com.example.model.GUEST_USER_ID
import com.example.model.SUPER_ADMIN_UID
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Active(
        val sessionId: String,
        val displayName: String,
        val email: String,
        val isGuest: Boolean,
        val isSuperAdmin: Boolean
    ) : SessionState
}

data class AuthUiState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(
    private val sessionPrefs: SessionPrefs,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val sessionState: StateFlow<SessionState> = combine(
        authStateFlow(),
        sessionPrefs.isGuestMode,
        sessionPrefs.guestName
    ) { user, isGuest, guestName ->
        when {
            user != null -> SessionState.Active(
                sessionId = user.uid,
                displayName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "চাষি ভাই",
                email = user.email.orEmpty(),
                isGuest = false,
                isSuperAdmin = user.uid == SUPER_ADMIN_UID
            )
            isGuest -> SessionState.Active(
                sessionId = GUEST_USER_ID,
                displayName = guestName,
                email = "অফলাইন / লোকাল মোড",
                isGuest = true,
                isSuperAdmin = false
            )
            else -> SessionState.Unauthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = SessionState.Loading
    )

    fun clearMessages() {
        _authUiState.value = _authUiState.value.copy(errorMessage = null, infoMessage = null)
    }

    fun signInWithEmail(email: String, password: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authUiState.value = AuthUiState(errorMessage = "অনুগ্রহ করে ইমেইল ও পাসওয়ার্ড লিখুন।")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isBusy = true)
            try {
                val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
                val user = result.user
                if (user != null) {
                    sessionPrefs.setGuestMode(false)
                    ensureUserProfileExists(user, fallbackName = cleanEmail.substringBefore("@"), farmName = "")
                }
                _authUiState.value = AuthUiState(isBusy = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("AuthVM", "Login failed", e)
                _authUiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = mapAuthErrorToBengali(e)
                )
            }
        }
    }

    fun signUpWithEmail(name: String, farmName: String, email: String, password: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isBlank()) {
            _authUiState.value = AuthUiState(errorMessage = "আপনার নাম লিখুন।")
            return
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            _authUiState.value = AuthUiState(errorMessage = "সঠিক ইমেইল ঠিকানা লিখুন।")
            return
        }
        if (password.length < 6) {
            _authUiState.value = AuthUiState(errorMessage = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isBusy = true)
            try {
                val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                val user = result.user
                if (user != null) {
                    try {
                        val profileReq = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                        user.updateProfile(profileReq).await()
                    } catch (_: Exception) {
                        // Non-fatal
                    }
                    sessionPrefs.setGuestMode(false)
                    ensureUserProfileExists(user, fallbackName = cleanName, farmName = farmName.trim())
                }
                _authUiState.value = AuthUiState(isBusy = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("AuthVM", "SignUp failed", e)
                _authUiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = mapAuthErrorToBengali(e)
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            _authUiState.value = AuthUiState(errorMessage = "পাসওয়ার্ড রিসেট করতে সঠিক ইমেইল লিখুন।")
            return
        }
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isBusy = true)
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
                _authUiState.value = AuthUiState(
                    isBusy = false,
                    infoMessage = "পাসওয়ার্ড রিসেট লিংক আপনার ইমেইলে ($cleanEmail) পাঠানো হয়েছে।"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authUiState.value = AuthUiState(
                    isBusy = false,
                    errorMessage = mapAuthErrorToBengali(e)
                )
            }
        }
    }

    fun enterGuestMode() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState(isBusy = true)
            sessionPrefs.setGuestMode(true, "অতিথি চাষি")
            _authUiState.value = AuthUiState(isBusy = false)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                try {
                    sessionPrefs.setGuestMode(false)
                    auth.signOut()
                } catch (e: Exception) {
                    Log.w("AuthVM", "Sign out warning", e)
                }
            }
        }
    }

    private suspend fun ensureUserProfileExists(
        user: FirebaseUser,
        fallbackName: String,
        farmName: String
    ) {
        val docPath = "users/${user.uid}"
        try {
            val docRef = firestore.collection("users").document(user.uid)
            val snap = docRef.get().await()
            val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: fallbackName.ifBlank { "চাষি" }
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "email" to (user.email ?: ""),
                    "displayName" to displayName.take(100),
                    "farmName" to farmName.take(120),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (farmName.isNotBlank()) {
                    updates["farmName"] = farmName.take(120)
                }
                docRef.set(updates, SetOptions.merge()).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
        }
    }

    private fun mapAuthErrorToBengali(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি। নতুন অ্যাকাউন্ট খুলুন অথবা গেস্ট মোড ব্যবহার করুন।"
        is FirebaseAuthInvalidCredentialsException -> "ইমেইল বা পাসওয়ার্ড সঠিক নয়। অনুগ্রহ করে আবার চেষ্টা করুন।"
        is FirebaseAuthUserCollisionException -> "এই ইমেইল দিয়ে আগেই অ্যাকাউন্ট খোলা আছে। অনুগ্রহ করে লগইন করুন।"
        else -> e.localizedMessage ?: "লগইন করতে সমস্যা হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
    }
}
