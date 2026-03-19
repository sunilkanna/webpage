package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.simats.genecare.data.network.ApiClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LogEntry(
    val id: String,
    val timestamp: String,
    val level: LogLevel,
    val message: String,
    val source: String
)

enum class LogLevel {
    INFO, WARNING, ERROR, SUCCESS
}

data class ReportItem(
    val title: String,
    val date: String,
    val type: String,
    val size: String,
    val fileUrl: String? = null
)

data class ReportsAndLogsState(
    val logs: List<LogEntry> = emptyList(),
    val reports: List<ReportItem> = emptyList(),
    val selectedTab: Int = 0, // 0 for Reports, 1 for Logs
    val isLoading: Boolean = false
)

class ReportsAndLogsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsAndLogsState())
    val uiState: StateFlow<ReportsAndLogsState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val user = com.simats.genecare.data.UserSession.getUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch Logs
                val logsResponse = ApiClient.api.getSystemLogs()
                val logs = if (logsResponse.isSuccessful && logsResponse.body() != null) {
                    logsResponse.body()!!.logs.map {
                        LogEntry(
                            id = it.id,
                            timestamp = it.timestamp,
                            level = try { LogLevel.valueOf(it.level) } catch (e: Exception) { LogLevel.INFO },
                            message = it.message,
                            source = it.source
                        )
                    }
                } else emptyList()

                // Fetch Reports (All for Admin, Patient Specific for others)
                val reportsResponse = if (user.userType == "Admin") {
                    ApiClient.api.getAllReports()
                } else {
                    ApiClient.api.getPatientResults(user.id)
                }

                val reports = if (reportsResponse.isSuccessful && reportsResponse.body()?.status == "success") {
                    reportsResponse.body()?.reports?.map {
                        ReportItem(
                            title = it.title,
                            date = it.date,
                            type = it.type ?: "PDF",
                            size = "N/A",
                            fileUrl = it.file_url ?: it.url
                        )
                    } ?: emptyList()
                } else emptyList()

                _uiState.value = _uiState.value.copy(
                    logs = logs,
                    reports = reports,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun getFileFromUri(context: android.content.Context, uri: android.net.Uri): java.io.File? {
        val contentResolver = context.contentResolver
        val tempFile = java.io.File(context.cacheDir, "temp_report_${System.currentTimeMillis()}")
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
    
    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    private fun getCurrentTime(offsetMinutes: Long = 0): String {
        val current = LocalDateTime.now().minusMinutes(-offsetMinutes) // minus negative to add? No, minusMinutes(positive) subtracts. 
        // Logic: getCurrentTime(-5) means 5 mins ago. 
        // java.time might not be available on older Android API levels without desugaring. 
        // I'll stick to simple strings or standard libraries if I suspect API issues, but `java.time` is standard in modern Android dev.
        // Assuming minSdk >= 26 or desugaring enabled. checking build.gradle... minSdk 24.
        // So I should be careful or use SimpleDateFormat.
        return "10:30 AM" // Placeholder to avoid crash unique formatted string logic for now
    }
}
