package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("notifications") val notifications: List<NotificationData>
)

data class NotificationData(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_read") val isRead: Int, // PHP returns 1 for true, 0 for false
    @SerializedName("created_at") val createdAt: String
)

data class MarkReadRequest(
    @SerializedName("notification_id") val notificationId: Int
)
