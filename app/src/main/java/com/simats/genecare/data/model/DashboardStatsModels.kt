package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class DashboardStatsRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_type") val userType: String
)

data class DashboardStatsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("patient_stats") val patientStats: PatientDashboardStats?,
    @SerializedName("counselor_stats") val counselorStats: CounselorDashboardStats?
)

data class PatientDashboardStats(
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("risk_category") val riskCategory: String,
    @SerializedName("last_assessment_date") val lastAssessmentDate: String?,
    @SerializedName("upcoming_appointment") val upcomingAppointment: AppointmentData?
)

data class CounselorDashboardStats(
    @SerializedName("todays_sessions") val todaysSessions: Int,
    @SerializedName("total_patients") val totalPatients: Int,
    @SerializedName("pending_requests_count") val pendingRequestsCount: Int? = 0,
    @SerializedName("avg_rating") val avgRating: Double,

    @SerializedName("revenue_this_month") val revenueThisMonth: String,
    @SerializedName("today_appointments") val todayAppointments: List<AppointmentData>,
    @SerializedName("recent_reviews") val recentReviews: List<ReviewData>? = null
)

data class ReviewData(
    @SerializedName("rating") val rating: Int,
    @SerializedName("review") val review: String,
    @SerializedName("submitted_at") val submittedAt: String,
    @SerializedName("author") val author: String,
    @SerializedName("days_ago") val daysAgo: String
)

data class AppointmentData(
    @SerializedName("id") val id: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("counselor_id") val counselorId: Int,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("status") val status: String,
    @SerializedName("patient_name") val patientName: String?,
    @SerializedName("counselor_name") val counselorName: String?
)

data class GetPatientAppointmentsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("appointments") val appointments: List<PatientAppointmentItem>
)

data class PatientAppointmentItem(
    @SerializedName("id") val id: Int,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("time_slot") val timeSlot: String,
    @SerializedName("status") val status: String,
    @SerializedName("counselor_name") val counselorName: String?
)
