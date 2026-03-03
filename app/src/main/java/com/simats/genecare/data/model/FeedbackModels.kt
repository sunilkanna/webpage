package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comments") val comments: String
)
