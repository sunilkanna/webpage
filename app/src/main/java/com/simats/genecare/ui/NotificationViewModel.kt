package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.UserSession
import com.simats.genecare.data.model.MarkReadRequest
import com.simats.genecare.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        val userId = UserSession.getUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.api.getNotifications(userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        val realNotifications = body.notifications.map { data ->
                            NotificationItem(
                                id = data.id.toString(),
                                title = data.title,
                                message = data.message,
                                time = formatTime(data.createdAt),
                                type = mapType(data.type),
                                isRead = data.isRead == 1
                            )
                        }
                        _notifications.value = realNotifications
                    } else {
                        _errorMessage.value = body?.status ?: "Unknown error"
                    }
                } else {
                    _errorMessage.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection failed: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _notifications.value.filter { !it.isRead }.forEach { notification ->
                try {
                    ApiClient.api.markNotificationRead(MarkReadRequest(notification.id.toInt()))
                } catch (e: Exception) {}
            }
            // Refresh list
            fetchNotifications()
        }
    }

    private fun mapType(backendType: String): NotificationType {
        return when (backendType) {
            "Appointment" -> NotificationType.SESSION_REMINDER
            "Message" -> NotificationType.MESSAGE
            "Verification" -> NotificationType.VERIFICATION_COMPLETE
            "Report" -> NotificationType.BACKUP // Using Backup icon for reports as it fits file upload
            else -> NotificationType.SYSTEM_ALERT
        }
    }

    private fun formatTime(createdAt: String): String {
        // Simple placeholder for time formatting logic
        // Ideally use a library or proper Date parsing
        return createdAt.substringBefore(" ").substringAfter("-") // Returns MM-DD for simplicity
    }
}
