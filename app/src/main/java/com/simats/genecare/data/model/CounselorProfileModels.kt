package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

// Counselor Profile Setup
data class SaveCounselorProfileRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("specialization") val specialization: String,
    @SerializedName("bio") val bio: String,
    @SerializedName("experience_years") val experienceYears: Int,
    @SerializedName("consultation_fee") val consultationFee: Double,

    @SerializedName("profile_image_url") val profileImageUrl: String?
)

data class UpdateConsultationFeeRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("consultation_fee") val consultationFee: Double
)

data class UpdateAppointmentStatusRequest(
    @SerializedName("appointment_id") val appointmentId: Int,
    @SerializedName("status") val status: String
)

data class CounselorProfileResponse(
    @SerializedName("status") val status: String,
    @SerializedName("profile") val profile: CounselorProfileData?
)

data class CounselorProfileData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("specialization") val specialization: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("experience_years") val experienceYears: Int?,
    @SerializedName("consultation_fee") val consultationFee: Double?,
    @SerializedName("profile_image_url") val profileImageUrl: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("status") val status: String?, // Added status from joined table
    @SerializedName("rejection_reason") val rejectionReason: String?
)

data class GenericResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("file_url") val fileUrl: String? = null,
    @SerializedName("email_sent") val emailSent: Boolean? = null,
    @SerializedName("email_error") val emailError: String? = null
)

// Counselor Qualifications
data class SaveQualificationRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("registration_number") val registrationNumber: String,
    @SerializedName("medical_council") val medicalCouncil: String,
    @SerializedName("registration_year") val registrationYear: String,
    @SerializedName("certificate_url") val certificateUrl: String
)

// Admin Verification
data class VerifyCounselorRequest(
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("counselor_id") val counselorId: Int,
    @SerializedName("status") val status: String, // Approved, Rejected
    @SerializedName("rejection_reason") val rejectionReason: String?
)

// Admin Dashboard Stats
data class AdminStatsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_patients") val totalPatients: Int,
    @SerializedName("active_counselors") val activeCounselors: Int,
    @SerializedName("pending_verifications") val pendingVerifications: Int,
    @SerializedName("system_alerts") val systemAlerts: Int
)

// Pending Counselors List for Admin
data class GetPendingCounselorsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("pending_counselors") val pendingCounselors: List<PendingCounselorData>
)

data class PendingCounselorData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("registration_number") val registrationNumber: String?,
    @SerializedName("medical_council") val medicalCouncil: String?,
    @SerializedName("registration_year") val registrationYear: String?,
    @SerializedName("certificate_url") val certificateUrl: String?,
    @SerializedName("submitted_at") val submittedAt: String?,
    @SerializedName("status") val status: String? // Added status to support approved/rejected in list
)

data class GetAllCounselorsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("all_counselors") val allCounselors: List<PendingCounselorData>
)

// Analytics
data class AnalyticsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("total_users") val totalUsers: String,
    @SerializedName("active_sessions") val activeSessions: String,
    @SerializedName("user_growth_data") val userGrowthData: List<Float>,
    @SerializedName("session_distribution_data") val sessionDistributionData: List<Float>,
    @SerializedName("session_distribution_labels") val sessionDistributionLabels: List<String>,
    @SerializedName("user_demographics_data") val userDemographicsData: List<Float>
)

// User Management
data class UserListResponse(
    @SerializedName("status") val status: String,
    @SerializedName("users") val users: List<UserData>
)

data class UserData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("status") val status: String
)

data class ManageUserRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("action") val action: String // delete, suspend, activate
)

// System Logs
data class SystemLogsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("logs") val logs: List<LogData>
)

data class LogData(
    @SerializedName("id") val id: String,
    @SerializedName("message") val message: String,
    @SerializedName("level") val level: String, // INFO, WARNING, ERROR, SUCCESS
    @SerializedName("source") val source: String,
    @SerializedName("timestamp") val timestamp: String
)
