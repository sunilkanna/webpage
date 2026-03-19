package com.simats.genecare.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.simats.genecare.data.repository.AuthRepository
import androidx.compose.ui.graphics.Color

data class CounselorQualificationData(
    val certificateUri: Uri? = null,
    val certificateFileName: String = "",
    val certificateFileSize: String = "",
    val doctorName: String = "",
    val registrationNumber: String = "",
    val yearOfRegistration: String = "",
    val stateMedicalCouncil: String = ""
)

class CounselorViewModel : ViewModel() {
    private val _qualificationData = MutableStateFlow(CounselorQualificationData())
    val qualificationData: StateFlow<CounselorQualificationData> = _qualificationData.asStateFlow()

    private val authRepository = AuthRepository()
    private val _submissionState = MutableStateFlow<String?>(null) // null, "Loading", "Success", "Error"
    val submissionState: StateFlow<String?> = _submissionState.asStateFlow()

    // Counselor Verification Logic
    data class Counselor(
        val id: String,
        val name: String,
        val email: String,
        val degree: String,
        val submitted: String,
        val fee: String,
        val status: String, // "Pending", "Approved", "Rejected"
        val rejectionReason: String? = null,
        val certificateUrl: String = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", // Dummy URL for demo
        val medicalCouncil: String = "",
        val registrationNumber: String = "",
        val registrationYear: String = ""
    )

    private val _counselors = MutableStateFlow<List<Counselor>>(emptyList())
    val counselors: StateFlow<List<Counselor>> = _counselors.asStateFlow()


    fun fetchAllCounselors() {
        viewModelScope.launch {
            try {
                // Use the new endpoint that returns ALL counselors (Pending, Approved, Rejected)
                val response = authRepository.getAllCounselorsAdmin()
                if (response.isSuccessful && response.body()?.status == "success") {
                    val allList = response.body()?.allCounselors ?: emptyList()
                    _counselors.value = allList.map {
                        Counselor(
                            id = it.userId.toString(),
                            name = it.fullName,
                            email = it.email,
                            degree = "${it.medicalCouncil ?: "Unknown"} (${it.registrationYear ?: "Unknown"})",
                            submitted = it.submittedAt ?: "Date Unavailable",
                            fee = "N/A",
                            status = it.status ?: "Pending", // Use status from DB or default to Pending
                            certificateUrl = it.certificateUrl ?: "",
                            medicalCouncil = it.medicalCouncil ?: "Not Provided",
                            registrationNumber = it.registrationNumber ?: "Not Provided",
                            registrationYear = it.registrationYear ?: "Not Provided"
                        )
                    }
                } else {
                    // Handle error or keep empty
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exception
            }
        }
    }



    // Simulate logged-in counselor (For demo purposes, we assume ID "1" is the current user)
    // Update: Try to get from UserSession, fallback to "1"
    private val _currentCounselorId = MutableStateFlow(com.simats.genecare.data.UserSession.getUserId()?.toString() ?: "1")
    
    // Updated: Fetch current counselor profile directly
    private val _currentCounselor = MutableStateFlow<Counselor?>(null)
    val currentCounselor: StateFlow<Counselor?> = _currentCounselor.asStateFlow()

    fun fetchCurrentCounselorProfile() {
        val currentId = _currentCounselorId.value.toIntOrNull() ?: 1
        viewModelScope.launch {
             try {
                 val response = authRepository.getCounselorProfile(currentId)
                 if (response.isSuccessful && response.body()?.status == "success") {
                     val profile = response.body()?.profile
                     if (profile != null) {
                         _currentCounselor.value = Counselor(
                             id = profile.userId.toString(),
                             name = profile.fullName ?: "",
                             email = profile.email ?: "",
                             degree = profile.specialization ?: "",
                             submitted = "", // Not needed for status check
                             fee = profile.consultationFee?.toString() ?: "",
                             status = profile.status ?: "Pending",
                             rejectionReason = profile.rejectionReason,
                             certificateUrl = ""
                         )
                         if (profile.status == "Approved") {
                             com.simats.genecare.data.UserSession.updateVerificationStatus("Approved")
                         }
                     }
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }

    private val _selectedCounselor = MutableStateFlow<Counselor?>(null)
    val selectedCounselor: StateFlow<Counselor?> = _selectedCounselor.asStateFlow()

    fun selectCounselor(counselor: Counselor) {
        _selectedCounselor.value = counselor
    }

    fun approveCounselor() {
        _selectedCounselor.value?.let { current ->
            viewModelScope.launch {
                try {
                    val adminId = com.simats.genecare.data.UserSession.getUserId() ?: 1
                    val response = authRepository.adminVerifyCounselor(
                        adminId = adminId,
                        counselorId = current.id.toInt(),
                        status = "Approved",
                        rejectionReason = null
                    )
                    if (response.isSuccessful && response.body()?.status == "success") {
                        fetchAllCounselors() // Refresh list
                        _selectedCounselor.value = _selectedCounselor.value?.copy(status = "Approved")
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun rejectCounselor(reason: String) {
        _selectedCounselor.value?.let { current ->
            viewModelScope.launch {
                try {
                    val adminId = com.simats.genecare.data.UserSession.getUserId() ?: 1
                    val response = authRepository.adminVerifyCounselor(
                        adminId = adminId,
                        counselorId = current.id.toInt(),
                        status = "Rejected",
                        rejectionReason = reason
                    )
                    if (response.isSuccessful && response.body()?.status == "success") {
                        fetchAllCounselors() // Refresh list
                        _selectedCounselor.value = _selectedCounselor.value?.copy(status = "Rejected", rejectionReason = reason)
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    fun updateFee(newFee: String) {
        _selectedCounselor.value?.let { current ->
            val feeDouble = newFee.toDoubleOrNull()
            if (feeDouble != null) {
                viewModelScope.launch {
                    try {
                        val response = authRepository.updateConsultationFee(current.id.toInt(), feeDouble)
                        if (response.isSuccessful && response.body()?.status == "success") {
                            _selectedCounselor.value = _selectedCounselor.value?.copy(fee = newFee)
                            fetchAllCounselors() // Refresh main list too
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    fun updateCertificate(uri: Uri?, fileName: String, fileSize: String) {
        _qualificationData.value = _qualificationData.value.copy(
            certificateUri = uri,
            certificateFileName = fileName,
            certificateFileSize = fileSize
        )
    }

    fun updateQualificationDetails(
        doctorName: String,
        registrationNumber: String,
        yearOfRegistration: String,
        stateMedicalCouncil: String
    ) {
        _qualificationData.value = _qualificationData.value.copy(
            doctorName = doctorName,
            registrationNumber = registrationNumber,
            yearOfRegistration = yearOfRegistration,
            stateMedicalCouncil = stateMedicalCouncil
        )
    }

    private fun getFileFromUri(context: android.content.Context, uri: Uri, fileName: String): java.io.File? {
        val contentResolver = context.contentResolver
        val extension = fileName.substringAfterLast('.', "")
        val suffix = if (extension.isNotEmpty()) ".$extension" else ""
        val tempFile = java.io.File(context.cacheDir, "temp_cert_${System.currentTimeMillis()}$suffix")
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun submitQualification(userId: Int, context: android.content.Context) {
        viewModelScope.launch {
            _submissionState.value = "Loading"
            try {
                val data = _qualificationData.value
                val uri = data.certificateUri
                val fileName = data.certificateFileName
                
                var certUrl = ""
                if (uri != null) {
                    val file = getFileFromUri(context, uri, fileName)
                    if (file != null) {
                        val uploadResponse = authRepository.uploadCertificate(file)
                        if (uploadResponse.isSuccessful && uploadResponse.body()?.status == "success") {
                            certUrl = uploadResponse.body()?.fileUrl ?: ""
                        } else {
                            _submissionState.value = "Error: File upload failed - ${uploadResponse.body()?.message ?: uploadResponse.message()}"
                            return@launch
                        }
                    } else {
                        _submissionState.value = "Error: Could not process certificate file"
                        return@launch
                    }
                }
                
                val response = authRepository.saveCounselorQualifications(
                    userId = userId,
                    doctorName = data.doctorName,
                    registrationNumber = data.registrationNumber,
                    medicalCouncil = data.stateMedicalCouncil,
                    registrationYear = data.yearOfRegistration,
                    certificateUrl = certUrl
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success") {
                        _submissionState.value = "Success"
                    } else {
                        _submissionState.value = "Error: ${body?.message ?: response.message()}"
                    }
                } else {
                    _submissionState.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _submissionState.value = "Error: ${e.message}"
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = null
    }




    // Session Requests Logic
    data class SessionRequest(
        val id: String,
        val name: String,
        val type: String,
        val date: String,
        val time: String,
        val reason: String,
        val imageInitial: String?,
        val imageColor: Color,
        val hasReport: Boolean,
        val reportUrl: String? = null,
        val status: String? = null
    )

    private val _sessionRequests = MutableStateFlow<List<SessionRequest>>(emptyList())
    val sessionRequests: StateFlow<List<SessionRequest>> = _sessionRequests.asStateFlow()

    private val _actionLoadingStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val actionLoadingStates: StateFlow<Map<String, Boolean>> = _actionLoadingStates.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _allAppointments = MutableStateFlow<List<SessionRequest>>(emptyList())
    val allAppointments: StateFlow<List<SessionRequest>> = _allAppointments.asStateFlow()

    init {
        // Observe UserSession changes to automatically fetch data for the correct user
        viewModelScope.launch {
            com.simats.genecare.data.UserSession.currentUser.collect { user ->
                if (user != null) {
                    _currentCounselorId.value = user.id.toString()
                    fetchCounselorAppointments()
                    fetchCurrentCounselorProfile()
                }
            }
        }
    }

    fun refresh() {
        fetchCounselorAppointments()
        fetchCurrentCounselorProfile()
    }



    fun fetchCounselorAppointments() {
        val currentId = com.simats.genecare.data.UserSession.getUserId() ?: 1
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                val response = authRepository.getCounselorAppointments(currentId)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val appointments = response.body()?.appointments ?: emptyList()
                    
                    val mappedAppointments = appointments.map {
                        SessionRequest(
                            id = it.id.toString(),
                            name = it.patientName ?: "Unknown Patient",
                            type = it.type ?: "Consultation",
                            date = it.date,
                            time = it.time,
                            reason = it.reason ?: "Routine Checkup",
                            imageInitial = it.imageInitial ?: "?",
                            imageColor = try { Color(android.graphics.Color.parseColor(it.imageColorHex ?: "#FFCC80")) } catch(e:Exception) { Color(0xFFFFCC80) },
                            hasReport = it.hasReport ?: false,
                            reportUrl = it.reportUrl,
                            status = it.status // Passing status to SessionRequest
                        )
                    }

                    // Save all appointments for the "View All" screen
                    _allAppointments.value = mappedAppointments

                    // Filter only Pending appointments for the Dashboard's "Session Requests" list
                    _sessionRequests.value = mappedAppointments.filter { 
                        it.status.isNullOrBlank() || it.status.equals("Pending", ignoreCase = true) 
                    }
                } else {
                    val errorMsg = response.body()?.message 
                        ?: response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "Failed to fetch session requests"
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Network error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun acceptSession(requestId: String) {
        viewModelScope.launch {
            _actionLoadingStates.value = _actionLoadingStates.value + (requestId to true)
            try {
                val response = authRepository.updateAppointmentStatus(requestId, "Confirmed")
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()
                    if (body?.emailSent == true) {
                        println("Confirmation email sent to patient successfully.")
                    } else if (body?.emailError != null) {
                        _error.value = "Session confirmed, but email failed: ${body.emailError}"
                    }
                    fetchCounselorAppointments()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _actionLoadingStates.value = _actionLoadingStates.value - requestId
            }
        }
    }

    fun rejectSession(requestId: String, reason: String) {
        viewModelScope.launch {
            _actionLoadingStates.value = _actionLoadingStates.value + (requestId to true)
            try {
                val response = authRepository.updateAppointmentStatus(requestId, "Cancelled", reason)
                if (response.isSuccessful && response.body()?.status == "success") {
                    fetchCounselorAppointments()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _actionLoadingStates.value = _actionLoadingStates.value - requestId
            }
        }
    }
}
