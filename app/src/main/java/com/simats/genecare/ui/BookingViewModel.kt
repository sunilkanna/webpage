package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.BookingRepository
import com.simats.genecare.data.Counselor
import com.simats.genecare.data.TimeSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val counselors: List<Counselor> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val selectedCounselor: Counselor? = null,
    val selectedDate: String = "1",
    val selectedTime: String? = "10:30 AM",
    val isBookingConfirmed: Boolean = false,
    val isSessionApproved: Boolean = false, 
    val uploadedReportName: String? = null,
    val uploadedReportUri: String? = null,
    // Dynamic Date Handling
    val currentYear: Int = 0,
    val currentMonth: Int = 0,
    val monthName: String = "",
    val daysInMonth: List<String> = emptyList(),
    // Today's date for validation
    val todayYear: Int = 0,
    val todayMonth: Int = 0,
    val todayDay: Int = 0,
    // Error message for user feedback
    val errorMessage: String? = null,
    // Live appointment tracking
    val lastBookedAppointmentId: Int? = null,
    val appointmentStatus: String = "Pending",
    val confirmedDate: String? = null,
    val confirmedTime: String? = null,
    val confirmedCounselorName: String? = null,
    val isLoadingStatus: Boolean = false,
    val isLoading: Boolean = false
)

class BookingViewModel(
    private val repository: BookingRepository = BookingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        // Initialize with actual current date
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH)
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

        // Store today's date for validation
        _uiState.value = _uiState.value.copy(
            todayYear = year,
            todayMonth = month,
            todayDay = day,
            selectedDate = day.toString()
        )
        updateDateState(year, month)

        viewModelScope.launch {
            fetchCounselors()
            _uiState.value = _uiState.value.copy(
                timeSlots = repository.getTimeSlots()
            )
        }
    }

    private fun fetchCounselors() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val counselorList = repository.fetchCounselors()
                if (counselorList.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No counselors found. Please check your connection or try again later.",
                        counselors = emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        counselors = counselorList,
                        selectedCounselor = counselorList.firstOrNull(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to fetch counselors: ${e.localizedMessage}"
                )
            }
        }
    }

    fun refreshCounselors() {
        fetchCounselors()
    }

    private fun updateDateState(year: Int, month: Int) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, year)
        cal.set(java.util.Calendar.MONTH, month)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)

        val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val days = (1..maxDays).map { it.toString() }
        val monthName = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.US) ?: "January"

        _uiState.value = _uiState.value.copy(
            currentYear = year,
            currentMonth = month,
            monthName = monthName,
            daysInMonth = days
        )
    }

    fun nextMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth + 1
        if (month > 11) {
            month = 0
            year++
        }
        updateDateState(year, month)
    }

    fun previousMonth() {
        var year = _uiState.value.currentYear
        var month = _uiState.value.currentMonth - 1
        if (month < 0) {
            month = 11
            year--
        }
        // Block navigation to months before the current month
        val state = _uiState.value
        if (year < state.todayYear || (year == state.todayYear && month < state.todayMonth)) {
            return
        }
        updateDateState(year, month)
    }

    fun selectCounselor(counselor: Counselor) {
        _uiState.value = _uiState.value.copy(selectedCounselor = counselor)
    }

    /**
     * Returns true if the given day (in the currently displayed month/year) is in the past.
     */
    fun isPastDate(day: Int): Boolean {
        val state = _uiState.value
        val displayYear = state.currentYear
        val displayMonth = state.currentMonth
        val todayYear = state.todayYear
        val todayMonth = state.todayMonth
        val todayDay = state.todayDay

        if (displayYear < todayYear) return true
        if (displayYear == todayYear && displayMonth < todayMonth) return true
        if (displayYear == todayYear && displayMonth == todayMonth && day < todayDay) return true
        return false
    }

    fun selectDate(date: String) {
        val dayNum = date.toIntOrNull() ?: return
        if (isPastDate(dayNum)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Cannot select a past date")
            return
        }
        _uiState.value = _uiState.value.copy(selectedDate = date, errorMessage = null)
    }

    fun selectTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Check if a time slot has already passed today.
     * Compares time like "10:30 AM" against the device's current time.
     */
    private fun isTimeSlotPastForToday(time: String): Boolean {
        val state = _uiState.value
        val selectedDay = state.selectedDate.toIntOrNull() ?: return false
        // Only check time if we're booking for today
        if (state.currentYear != state.todayYear || state.currentMonth != state.todayMonth || selectedDay != state.todayDay) {
            return false
        }
        return try {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)
            val slotTime = sdf.parse(time) ?: return false
            val now = java.util.Calendar.getInstance()
            val slotCal = java.util.Calendar.getInstance()
            slotCal.time = slotTime
            slotCal.set(java.util.Calendar.YEAR, now.get(java.util.Calendar.YEAR))
            slotCal.set(java.util.Calendar.MONTH, now.get(java.util.Calendar.MONTH))
            slotCal.set(java.util.Calendar.DAY_OF_MONTH, now.get(java.util.Calendar.DAY_OF_MONTH))
            slotCal.before(now)
        } catch (e: Exception) {
            false
        }
    }

    fun confirmBooking() {
        val state = _uiState.value
        if (state.selectedCounselor != null && state.selectedTime != null) {
            // Validate date is not in the past
            val selectedDay = state.selectedDate.toIntOrNull()
            if (selectedDay != null && isPastDate(selectedDay)) {
                _uiState.value = _uiState.value.copy(errorMessage = "Cannot book a session for a past date")
                return
            }

            // Validate time slot hasn't passed (for today)
            if (isTimeSlotPastForToday(state.selectedTime)) {
                _uiState.value = _uiState.value.copy(errorMessage = "This time slot has already passed. Please select a later time.")
                return
            }

            viewModelScope.launch {
                val appointmentId = repository.bookSession(
                    counselorId = state.selectedCounselor.id,
                    date = "${state.monthName} ${state.selectedDate}, ${state.currentYear}",
                    time = state.selectedTime,
                    reportUrl = state.uploadedReportUri
                )
                _uiState.value = _uiState.value.copy(
                    isBookingConfirmed = true,
                    lastBookedAppointmentId = appointmentId,
                    appointmentStatus = "Pending",
                    isSessionApproved = false,
                    errorMessage = null
                )
            }
        }
    }

    fun fetchAppointmentStatus(passedId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStatus = true)
            try {
                // First ensure counselors are loaded
                if (_uiState.value.counselors.isEmpty()) {
                    val list = repository.fetchCounselors()
                    _uiState.value = _uiState.value.copy(counselors = list)
                }

                var appointmentId = passedId ?: _uiState.value.lastBookedAppointmentId

                // If no explicit appointment ID, auto-discover the patient's latest appointment
                if (appointmentId == null || appointmentId == 0) {
                    val patientId = com.simats.genecare.data.UserSession.getUser()?.id
                    if (patientId != null) {
                        val listResponse = com.simats.genecare.data.network.ApiClient.api.getPatientAppointments(patientId)
                        if (listResponse.isSuccessful && listResponse.body()?.status == "success") {
                            val appointments = listResponse.body()?.appointments ?: emptyList()
                            // Find latest non-cancelled appointment
                            val latest = appointments
                                .filter { !it.status.equals("Cancelled", ignoreCase = true) }
                                .maxByOrNull { it.id }
                            if (latest != null) {
                                appointmentId = latest.id
                                _uiState.value = _uiState.value.copy(lastBookedAppointmentId = appointmentId)
                            }
                        }
                    }
                }

                if (appointmentId == null || appointmentId == 0) {
                    _uiState.value = _uiState.value.copy(isLoadingStatus = false)
                    return@launch
                }

                val details = repository.fetchAppointmentDetails(appointmentId)
                if (details != null) {
                    val isConfirmed = details.status.equals("Confirmed", ignoreCase = true)
                    
                    // Crucial: Find the actual counselor object from our list and set it
                    val counselor = _uiState.value.counselors.find { it.id == details.counselorId.toString() }
                    
                    _uiState.value = _uiState.value.copy(
                        isSessionApproved = isConfirmed,
                        appointmentStatus = details.status,
                        confirmedDate = details.appointmentDate,
                        confirmedTime = details.timeSlot,
                        confirmedCounselorName = details.counselorName,
                        selectedCounselor = counselor, // Update the counselor object for consistent UI (initials, color)
                        isLoadingStatus = false
                        // isBookingConfirmed = true // REMOVED: This was causing navigation loops on screen refresh
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingStatus = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoadingStatus = false)
            }
        }
    }

    fun onReportSelected(uri: String, name: String) {
        _uiState.value = _uiState.value.copy(
            uploadedReportUri = uri,
            uploadedReportName = name
        )
    }

    fun setAppointmentId(id: Int) {
        _uiState.value = _uiState.value.copy(lastBookedAppointmentId = id)
    }

    fun removeReport() {
        _uiState.value = _uiState.value.copy(
            uploadedReportUri = null,
            uploadedReportName = null
        )
    }


    fun resetBookingState() {
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH)
        // Reset to initial state but fetch counselors
        viewModelScope.launch {
            val counselorList = repository.fetchCounselors()
            
            // Create base state
            _uiState.value = BookingUiState(
                 counselors = counselorList,
                 timeSlots = repository.getTimeSlots(),
                 selectedCounselor = counselorList.firstOrNull()
            )
            // Apply date logic
            updateDateState(year, month)
        }
    }
}
