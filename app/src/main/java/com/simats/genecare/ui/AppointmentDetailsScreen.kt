package com.simats.genecare.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.genecare.data.Counselor
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsScreen(
    navController: NavController,
    viewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    appointmentId: Int? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val counselor = uiState.selectedCounselor

    // Fetch live appointment status from backend when screen loads
    LaunchedEffect(appointmentId) {
        if (appointmentId != null && appointmentId != 0) {
            viewModel.setAppointmentId(appointmentId)
        }
        viewModel.fetchAppointmentStatus(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Appointment Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B2A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8FBFF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoadingStatus) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF00ACC1))
                }
            } else {
                // Status Card
                StatusCard(isApproved = uiState.isSessionApproved)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Counselor Details Card - use live data if available, fallback to local
            val displayDate = uiState.confirmedDate ?: "${uiState.monthName} ${uiState.selectedDate}, ${uiState.currentYear}"
            val displayTime = uiState.confirmedTime ?: uiState.selectedTime ?: "--:--"
            val displayCounselorName = uiState.confirmedCounselorName

            if (counselor != null) {
                CounselorDetailsCard(
                    counselor = counselor,
                    date = displayDate,
                    time = displayTime
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Note Card
            NoteCard(counselorName = displayCounselorName ?: counselor?.name ?: "the counselor", isApproved = uiState.isSessionApproved)

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Button(
                onClick = { 
                    if (uiState.isSessionApproved) {
                        val apptId = uiState.lastBookedAppointmentId ?: 0
                        navController.navigate("video_call/$apptId")
                    }
                },
                enabled = uiState.isSessionApproved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1),
                    disabledContainerColor = Color(0xFFD1D5DB),
                    disabledContentColor = Color(0xFF6B7280)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isSessionApproved) "Join Session" else "Join Session (Pending Confirmation)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { 
                    viewModel.resetBookingState()
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00ACC1))
            ) {
                Text(
                    "Back to Home",
                    color = Color(0xFF00ACC1),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatusCard(isApproved: Boolean) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(if (isApproved) Color(0xFFE0F2F1) else Color(0xFFFFF7ED), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = if (isApproved) Color(0xFF009688) else Color(0xFFF97316),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isApproved) "Appointment Confirmed" else "Waiting for Counselor Approval",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isApproved) "Your appointment has been confirmed. You can join the session at the scheduled time." else "Your booking request has been sent to the counselor. We'll notify you once they respond.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF6B7280),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun CounselorDetailsCard(
    counselor: Counselor,
    date: String,
    time: String
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(counselor.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = counselor.initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = counselor.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B2A)
                    )
                    Text(
                        text = counselor.specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${counselor.rating} (127 reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(24.dp))

            DetailItem(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = date,
                iconColor = Color(0xFFE0F2FE),
                tintColor = Color(0xFF0EA5E9)
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailItem(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = "$time - ${calculateEndTime(time)}",
                iconColor = Color(0xFFF0FDF4),
                tintColor = Color(0xFF22C55E)
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailItem(
                icon = Icons.Default.VideoCameraFront,
                label = "Session Type",
                value = "Video Consultation",
                iconColor = Color(0xFFF5F3FF),
                tintColor = Color(0xFF8B5CF6)
            )
        }
    }
}

fun calculateEndTime(startTime: String): String {
    // Simple helper to add 1 hour to the start time string (e.g., "10:30 AM" -> "11:30 AM")
    return try {
        val parts = startTime.split(" ")
        val timeParts = parts[0].split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1]
        val period = parts[1]
        
        var nextHour = hour + 1
        var nextPeriod = period
        
        if (nextHour == 12) {
            nextPeriod = if (period == "AM") "PM" else "AM"
        } else if (nextHour > 12) {
            nextHour = 1
        }
        
        "$nextHour:$minute $nextPeriod"
    } catch (e: Exception) {
        "--:--"
    }
}

@Composable
fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    tintColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
fun NoteCard(counselorName: String, isApproved: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isApproved) Color(0xFFE0F2F1) else Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, if (isApproved) Color(0xFFB2DFDB) else Color(0xFFFEF3C7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Please Note:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isApproved) Color(0xFF00695C) else Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isApproved) "Please join the session 5 minutes before the scheduled time." else "This appointment is pending counselor confirmation. You'll receive a notification once $counselorName accepts your request.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isApproved) Color(0xFF004D40) else Color(0xFFB45309),
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentDetailsScreenPreview() {
    GenecareTheme {
        AppointmentDetailsScreen(NavController(androidx.compose.ui.platform.LocalContext.current))
    }
}
