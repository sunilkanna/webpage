package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.UserSession
import com.simats.genecare.data.model.EndSessionRequest
import com.simats.genecare.data.model.StartSessionRequest
import com.simats.genecare.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoCallState(
    val isLoading: Boolean = true,
    val meetingLink: String? = null,
    val counselorName: String = "Doctor",
    val patientName: String = "Patient",
    val callStatus: String = "Connecting...",
    val sessionEnded: Boolean = false,
    val sessionDurationMinutes: Int = 0,
    val consultationFee: Double = 0.0,
    val jwt: String? = null,
    val medicalReportUrl: String? = null,
    val patientReports: List<com.simats.genecare.data.model.SessionReport> = emptyList(),
    val secondsUntilSession: Long = 0L,
    val appointmentTime: String? = null,
    val errorMessage: String? = null
)

class VideoCallViewModel : ViewModel() {

    private val _state = MutableStateFlow(VideoCallState())
    val state: StateFlow<VideoCallState> = _state.asStateFlow()

    // Keep legacy flows for backward compatibility
    private val _appointmentDetails = MutableStateFlow<com.simats.genecare.data.model.AppointmentDetailData?>(null)
    val appointmentDetails: StateFlow<com.simats.genecare.data.model.AppointmentDetailData?> = _appointmentDetails.asStateFlow()

    private val _callStatus = MutableStateFlow<String>("Connecting...")
    val callStatus: StateFlow<String> = _callStatus.asStateFlow()

    private var currentAppointmentId: Int? = null

    fun startSession(appointmentId: Int) {
        currentAppointmentId = appointmentId
        val user = UserSession.getUser() ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val request = StartSessionRequest(
                    appointmentId = appointmentId,
                    userId = user.id
                )
                val response = ApiClient.api.startSession(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()!!
                    if (!body.jwtError.isNullOrEmpty() && body.jwt.isNullOrEmpty()) {
                        val msg = "JWT Error: ${body.jwtError}"
                        _state.value = _state.value.copy(isLoading = false, callStatus = msg, errorMessage = msg)
                        _callStatus.value = msg
                    } else {
                        val jwtParam = if (!body.jwt.isNullOrEmpty()) "?jwt=${body.jwt}" else ""
                        val baseMeetingLink = body.jitsiDirectLink ?: body.meetingLink
                        _state.value = _state.value.copy(
                            isLoading = false,
                            meetingLink = baseMeetingLink + "$jwtParam#config.disableDeepLinking=true&config.prejoinPageEnabled=false&config.startWithAudioMuted=false&config.startWithVideoMuted=false&config.requireDisplayName=true&config.p2p.enabled=false&config.disableSelfView=false&interfaceConfig.TOOLBAR_BUTTONS=[]&config.toolbarButtons=[]&config.disableTileView=true&config.hideConferenceTimer=true&config.hideParticipantsStats=true&config.disableRemoteMute=true",
                            jwt = body.jwt,
                            counselorName = body.counselorName ?: "Doctor",
                            patientName = body.patientName ?: "Patient",
                                medicalReportUrl = body.medicalReportUrl,
                                patientReports = body.patientReports ?: emptyList(),
                                secondsUntilSession = 0L,
                                callStatus = "Connected"
                            )
                            _callStatus.value = "Connected"
                        }
                    } else if (response.body()?.status == "too_early") {
                        val body = response.body()!!
                        _state.value = _state.value.copy(
                            isLoading = false,
                            secondsUntilSession = body.secondsUntilStart ?: 0L,
                            appointmentTime = body.appointmentTime,
                            counselorName = body.counselorName ?: "Doctor",
                            patientName = body.patientName ?: "Patient",
                            callStatus = "Too Early"
                        )
                        startCountdown()
                    } else {
                    // Try to get error message from response body or error body
                    val msg = response.body()?.message
                        ?: try {
                            val errorJson = response.errorBody()?.string()
                            org.json.JSONObject(errorJson ?: "{}").optString("message", "Unknown error (HTTP ${response.code()})")
                        } catch (e: Exception) {
                            "Failed to start session (HTTP ${response.code()})"
                        }
                    _state.value = _state.value.copy(isLoading = false, callStatus = msg, errorMessage = msg)
                    _callStatus.value = msg
                }
            } catch (e: Exception) {
                val msg = "Error: ${e.localizedMessage}"
                _state.value = _state.value.copy(isLoading = false, callStatus = msg, errorMessage = msg)
                _callStatus.value = msg
            }
            
            // Start polling for session completion (mainly for patients)
            startStatusPolling(appointmentId)
        }
    }

    private var pollingJob: kotlinx.coroutines.Job? = null

    private fun startStatusPolling(appointmentId: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds
                try {
                    val response = ApiClient.api.checkSessionReady(appointmentId)
                    if (response.isSuccessful && response.body()?.status == "success") {
                        if (response.body()?.appointmentStatus == "Completed") {
                            _state.value = _state.value.copy(sessionEnded = true)
                            // We might need to fetch final bill details here if needed
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var countdownJob: kotlinx.coroutines.Job? = null

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.secondsUntilSession > 0) {
                kotlinx.coroutines.delay(1000)
                val currentSeconds = _state.value.secondsUntilSession
                if (currentSeconds <= 1L) {
                    _state.value = _state.value.copy(secondsUntilSession = 0L)
                    break
                }
                _state.value = _state.value.copy(secondsUntilSession = currentSeconds - 1)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        countdownJob?.cancel()
    }

    fun endCall(appointmentId: Int, onCallEnded: () -> Unit) {
        val user = UserSession.getUser() ?: return
        viewModelScope.launch {
            try {
                val request = EndSessionRequest(
                    appointmentId = appointmentId,
                    userId = user.id
                )
                val response = ApiClient.api.endSession(request)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()!!
                    _state.value = _state.value.copy(
                        sessionEnded = true,
                        sessionDurationMinutes = body.sessionDurationMinutes ?: 0,
                        consultationFee = body.consultationFee ?: 0.0
                    )
                }
                onCallEnded()
            } catch (e: Exception) {
                onCallEnded()
            }
        }
    }

    // Legacy method for backward compatibility
    fun fetchAppointmentDetails(appointmentId: Int) {
        startSession(appointmentId)
    }
}
