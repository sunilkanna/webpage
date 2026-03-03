package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class GetAppointmentDetailsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("appointment") val appointment: AppointmentDetailData?,
    @SerializedName("message") val message: String? = null
)

data class AppointmentDetailData(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("counselor_id") val counselorId: Int,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("status") val status: String,
    @SerializedName("meeting_link") val meetingLink: String? = null,
    @SerializedName("session_start_time") val sessionStartTime: String? = null,
    @SerializedName("session_end_time") val sessionEndTime: String? = null,
    @SerializedName("session_duration_minutes") val sessionDurationMinutes: Int? = 0,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("counselor_name") val counselorName: String?,
    @SerializedName("consultation_fee") val consultationFee: Double?,
    @SerializedName("medical_report_url") val medicalReportUrl: String?
)

data class CompleteAppointmentRequest(
    @SerializedName("appointment_id") val appointmentId: Int
)

data class CreatePaymentRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("payment_method") val paymentMethod: String
)

data class CreatePaymentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("payment_id") val paymentId: Int?,
    @SerializedName("razorpay_order_id") val razorpayOrderId: String? = null,
    @SerializedName("amount") val amount: Int? = null, // Amount in paise
    @SerializedName("key_id") val keyId: String? = null,
    @SerializedName("message") val message: String?
)

data class VerifyPaymentSignatureRequest(
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("razorpay_order_id") val razorpayOrderId: String,
    @SerializedName("razorpay_payment_id") val razorpayPaymentId: String,
    @SerializedName("razorpay_signature") val razorpaySignature: String
)

data class UpdatePaymentStatusRequest(
    @SerializedName("payment_id") val paymentId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("transaction_id") val transactionId: String?
)

// Session Management Models
data class StartSessionRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("user_id") val userId: Int
)

data class StartSessionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("meeting_link") val meetingLink: String?,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("counselor_name") val counselorName: String?,
    @SerializedName("appointment_date") val appointmentDate: String?,
    @SerializedName("time_slot") val timeSlot: String?,
    @SerializedName("medical_report_url") val medicalReportUrl: String? = null,
    @SerializedName("jwt") val jwt: String? = null,
    @SerializedName("jwt_error") val jwtError: String? = null,
    @SerializedName("is_moderator") val isModerator: Boolean? = false
)

data class EndSessionRequest(
    @SerializedName("appointment_id") val appointmentId: Int
)

data class EndSessionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("session_duration_minutes") val sessionDurationMinutes: Int?,
    @SerializedName("consultation_fee") val consultationFee: Double?,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("counselor_name") val counselorName: String?
)

data class CheckSessionReadyResponse(
    @SerializedName("status") val status: String,
    @SerializedName("appointment_status") val appointmentStatus: String?,
    @SerializedName("session_started") val sessionStarted: Boolean?,
    @SerializedName("meeting_link") val meetingLink: String?
)

