package com.simats.genecare.data

import com.simats.genecare.data.model.BookAppointmentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

class BookingRepository {
    // We will now load this from API
    private var _counselors = emptyList<Counselor>()

    private val _timeSlots = listOf(
        TimeSlot("9:00 AM"),
        TimeSlot("10:30 AM"),
        TimeSlot("2:00 PM"),
        TimeSlot("3:30 PM"),
        TimeSlot("5:00 PM")
    )

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    // Now a suspend function since it calls API
    suspend fun fetchCounselors(): List<Counselor> {
        return try {
            val response = com.simats.genecare.data.network.ApiClient.api.getCounselors()
            if (response.isSuccessful && response.body()?.status == "success") {
                val dtoList = response.body()?.counselors ?: emptyList()
                _counselors = dtoList.map { dto ->
                    Counselor(
                        id = dto.id.toString(),
                        name = dto.fullName,
                        specialty = dto.specialization ?: "Genetic Counselor",
                        rating = dto.rating ?: 0.0,
                        initials = getInitials(dto.fullName),
                        colorHex = getColorForUser(dto.id) // Helper to generate consistent color
                    )
                }
                _counselors
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // For synchronous access if needed (after fetch), or just to satisfy ViewModel if it calls this synchronously (ViewModel needs update)
    fun getCounselorsSync(): List<Counselor> = _counselors
    
    fun getTimeSlots(): List<TimeSlot> = _timeSlots

    suspend fun bookSession(
        counselorId: String, 
        date: String, 
        time: String, 
        reportUrl: String? = null,
        reason: String? = "General Consultation",
        type: String? = "Video Call"
    ): Int? {
        return try {
            val patientId = UserSession.getUser()?.id ?: 1
            val cId = counselorId.toIntOrNull() ?: 1
            
            // Format date to YYYY-MM-DD
            val inputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formattedDate = try {
                val parsedDate = inputFormat.parse(date)
                if (parsedDate != null) {
                    outputFormat.format(parsedDate)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
                }
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date()) 
            }

            val request = BookAppointmentRequest(
                patientId = patientId,
                counselorId = cId,
                date = formattedDate,
                time = time,
                medicalReportUrl = reportUrl,
                reason = reason,
                appointmentType = type
            )

            val response = com.simats.genecare.data.network.ApiClient.api.bookAppointment(request)
            
            if (response.isSuccessful && response.body()?.status == "success") {
               response.body()?.appointmentId
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchAppointmentDetails(appointmentId: Int): com.simats.genecare.data.model.AppointmentDetailData? {
        return try {
            val response = com.simats.genecare.data.network.ApiClient.api.getAppointmentDetails(appointmentId)
            if (response.isSuccessful && response.body()?.status == "success") {
                response.body()?.appointment
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getInitials(name: String): String {
        return name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .mapNotNull { it.firstOrNull() }
            .joinToString("")
            .uppercase()
    }

    private fun getColorForUser(id: Int): Long {
        val colors = listOf(0xFF00ACC1, 0xFF00BFA5, 0xFF00BDD6, 0xFF7E57C2, 0xFF5C6BC0, 0xFFEC407A)
        return colors[id % colors.size]
    }
}
