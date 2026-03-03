package com.simats.genecare.data

import com.simats.genecare.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private var sessionManager: SessionManager? = null

    fun init(context: android.content.Context) {
        sessionManager = SessionManager(context.applicationContext)
        _currentUser.value = sessionManager?.getUserSession()
    }

    fun login(user: User) {
        sessionManager?.saveUserSession(user)
        _currentUser.value = user
    }

    fun logout() {
        sessionManager?.clearSession()
        _currentUser.value = null
    }

    fun updateVerificationStatus(status: String) {
        _currentUser.value?.let { user ->
            val updatedUser = user.copy(verificationStatus = status)
            sessionManager?.saveUserSession(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun getUserId(): Int? {
        return _currentUser.value?.id
    }
    
    fun isLoggedIn(): Boolean {
        return _currentUser.value != null
    }
    
    fun getUserType(): String? {
        return _currentUser.value?.userType
    }

    fun getUser(): User? {
        return _currentUser.value
    }
}
