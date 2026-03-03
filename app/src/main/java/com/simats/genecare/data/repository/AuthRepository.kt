package com.simats.genecare.data.repository

import com.simats.genecare.data.model.AuthResponse
import com.simats.genecare.data.model.LoginRequest
import com.simats.genecare.data.model.RegisterRequest
import com.simats.genecare.data.network.ApiClient
import retrofit2.Response

import com.simats.genecare.data.model.GenericResponse
import com.simats.genecare.data.model.SaveQualificationRequest
import com.simats.genecare.data.model.GetPendingCounselorsResponse
import com.simats.genecare.data.model.VerifyCounselorRequest
import com.simats.genecare.data.model.AdminStatsResponse
import com.simats.genecare.data.model.GetAllCounselorsResponse

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AuthRepository {
    private val api = ApiClient.api

    suspend fun register(fullName: String, email: String, password: String, userType: String): Response<AuthResponse> {
        return api.register(fullName, email, password, userType)
    }

    suspend fun login(loginRequest: LoginRequest): Response<AuthResponse> {
        return api.login(loginRequest.email, loginRequest.password)
    }

    suspend fun saveCounselorQualifications(userId: Int, doctorName: String, registrationNumber: String, medicalCouncil: String, registrationYear: String, certificateUrl: String): Response<GenericResponse> {
        val request = SaveQualificationRequest(userId, doctorName, registrationNumber, medicalCouncil, registrationYear, certificateUrl)
        return api.saveCounselorQualifications(request)
    }

    suspend fun getPendingCounselors(): Response<GetPendingCounselorsResponse> {
        return api.getPendingCounselors()
    }

    suspend fun adminVerifyCounselor(adminId: Int, counselorId: Int, status: String, rejectionReason: String?): Response<GenericResponse> {
        val request = VerifyCounselorRequest(adminId, counselorId, status, rejectionReason)
        return api.adminVerifyCounselor(request)
    }

    suspend fun getAdminStats(): Response<AdminStatsResponse> {
        return api.getAdminStats()
    }


    suspend fun getAllCounselorsAdmin(): Response<GetAllCounselorsResponse> {
        return api.getAllCounselorsAdmin()
    }

    suspend fun getCounselorAppointments(counselorId: Int): Response<com.simats.genecare.data.model.GetCounselorAppointmentsResponse> {
        return api.getCounselorAppointments(counselorId)
    }


    suspend fun getCounselorReports(counselorId: Int): Response<com.simats.genecare.data.model.GetCounselorReportsResponse> {
        return api.getCounselorReports(counselorId)
    }


    suspend fun getCounselorPatients(counselorId: Int): Response<com.simats.genecare.data.model.GetCounselorPatientsResponse> {
        return api.getCounselorPatients(counselorId)
    }


    suspend fun getCounselorProfile(userId: Int): Response<com.simats.genecare.data.model.CounselorProfileResponse> {
        return api.getCounselorProfile(userId)
    }

    suspend fun updateConsultationFee(userId: Int, fee: Double): Response<GenericResponse> {
        val request = com.simats.genecare.data.model.UpdateConsultationFeeRequest(userId, fee)
        return api.updateConsultationFee(request)
    }

    // Video Call & Billing
    suspend fun getAppointmentDetails(appointmentId: Int): Response<com.simats.genecare.data.model.GetAppointmentDetailsResponse> {
        return api.getAppointmentDetails(appointmentId)
    }

    suspend fun completeAppointment(appointmentId: Int): Response<com.simats.genecare.data.model.GenericResponse> {
        return api.completeAppointment(com.simats.genecare.data.model.CompleteAppointmentRequest(appointmentId))
    }

    suspend fun createPayment(appointmentId: Int, amount: Double, method: String): Response<com.simats.genecare.data.model.CreatePaymentResponse> {
        return api.createPayment(com.simats.genecare.data.model.CreatePaymentRequest(appointmentId, amount, method))
    }


    suspend fun updatePaymentStatus(paymentId: Int, status: String, transactionId: String?): Response<com.simats.genecare.data.model.GenericResponse> {
        return api.updatePaymentStatus(com.simats.genecare.data.model.UpdatePaymentStatusRequest(paymentId, status, transactionId))
    }

    suspend fun verifyPaymentSignature(paymentId: Int, razorpayOrderId: String, razorpayPaymentId: String, razorpaySignature: String): Response<com.simats.genecare.data.model.GenericResponse> {
        return api.verifyPaymentSignature(com.simats.genecare.data.model.VerifyPaymentSignatureRequest(paymentId, razorpayOrderId, razorpayPaymentId, razorpaySignature))
    }

    suspend fun getCounselorAnalytics(counselorId: Int): Response<com.simats.genecare.data.model.CounselorAnalyticsResponse> {
        return api.getCounselorAnalytics(counselorId)
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Response<GenericResponse> {
        val request = com.simats.genecare.data.model.UpdateAppointmentStatusRequest(appointmentId.toInt(), status)
        return api.updateAppointmentStatus(request)
    }

    suspend fun uploadReport(patientId: Int, file: java.io.File): Response<GenericResponse> {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
        val patientIdPart = patientId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return api.uploadReport(patientIdPart, body)
    }

    suspend fun submitFeedback(appointmentId: Int, patientId: Int, rating: Int, comments: String): Response<GenericResponse> {
        return api.submitFeedback(com.simats.genecare.data.model.FeedbackRequest(appointmentId, patientId, rating, comments))
    }
}
