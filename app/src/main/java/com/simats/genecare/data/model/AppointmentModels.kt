package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class BookAppointmentRequest(
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("counselor_id") val counselorId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("time") val time: String,
    @SerializedName("medical_report_url") val medicalReportUrl: String? = null,
    @SerializedName("reason") val reason: String? = "General Consultation",
    @SerializedName("appointment_type") val appointmentType: String? = "Video Call"
)

data class BookAppointmentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("appointment_id") val appointmentId: Int?
)

data class CounselorResponse(
    @SerializedName("status") val status: String,
    @SerializedName("counselors") val counselors: List<CounselorDto>
)

data class CounselorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_image_url") val profileImageUrl: String?,
    @SerializedName("specialization") val specialization: String?,
    @SerializedName("consultation_fee") val consultationFee: Double? = null,
    @SerializedName("rating") val rating: Double?
)
