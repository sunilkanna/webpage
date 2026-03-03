package com.simats.genecare.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import com.simats.genecare.data.Counselor
import com.simats.genecare.data.TimeSlot
import com.simats.genecare.ui.theme.GenecareTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSessionScreen(
    navController: NavController,
    viewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show error messages via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    if (uiState.isBookingConfirmed) {
        // Reset state first so re-opening BookSessionScreen doesn't immediately jump away
        LaunchedEffect(Unit) {
            viewModel.resetBookingState()
            navController.navigate("appointment_details") {
                popUpTo("book_session") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Book Counseling Session",
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
        containerColor = Color(0xFFF8FBFF),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Select Counselor
            Text(
                "Select Counselor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00ACC1))
                }
            } else if (uiState.counselors.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No counselors available at the moment.",
                            color = Color(0xFFE65100),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.refreshCounselors() }) {
                            Text("Tap to Retry", color = Color(0xFF00ACC1), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                uiState.counselors.forEach { counselor ->
                    CounselorCard(
                        counselor = counselor,
                        isSelected = uiState.selectedCounselor?.id == counselor.id,
                        onSelect = { viewModel.selectCounselor(counselor) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Date
            Text(
                "Select Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            CalendarCard(
                monthName = uiState.monthName,
                year = uiState.currentYear,
                days = uiState.daysInMonth,
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) },
                onPrevMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(24.dp))
            // Available Time Slots
            Text(
                "Available Time Slots",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            TimeSlotGrid(
                timeSlots = uiState.timeSlots,
                selectedTime = uiState.selectedTime,
                onTimeSelected = { viewModel.selectTime(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upload Report Section
            Text(
                "Upload Medical Report (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            UploadReportSection(
                fileName = uiState.uploadedReportName,
                onUploadClick = { 
                    // This will be handled by the launcher in the main composable
                },
                onRemoveClick = { viewModel.removeReport() },
                launcher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        val fileName = it.lastPathSegment ?: "medical_report.pdf"
                        viewModel.onReportSelected(it.toString(), fileName)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Booking Button
            Button(
                onClick = { viewModel.confirmBooking() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00ACC1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Confirm Booking",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CounselorCard(
    counselor: Counselor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF00ACC1)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(counselor.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = counselor.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = counselor.name,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B2A)
                )
                Text(
                    text = counselor.specialty,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = counselor.rating.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
@Composable
fun CalendarCard(
    monthName: String,
    year: Int,
    days: List<String>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    viewModel: BookingViewModel? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, null) }
                Text(
                    "$monthName $year",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B2A)
                )
                IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null) }
            }

            // Simplified grid logic
            Column {
                // Ensure we have enough rows to display all days
                // 7 cols per row.
                val rows = (days.size + 6) / 7
                
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 0..6) {
                            val index = row * 7 + col
                            if (index < days.size) {
                                val day = days[index]
                                val dayNum = day.toIntOrNull()
                                val isPast = dayNum != null && viewModel?.isPastDate(dayNum) == true
                                val isSelected = day == selectedDate
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected && !isPast -> Color(0xFF00ACC1)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (!isPast) Modifier.clickable { onDateSelected(day) }
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = when {
                                            isPast -> Color(0xFFBDBDBD)
                                            isSelected -> Color.White
                                            else -> Color(0xFF0D1B2A)
                                        },
                                        fontWeight = if (isSelected && !isPast) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun TimeSlotGrid(
    timeSlots: List<TimeSlot>,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit
) {
    // We'll use a Flow-like layout or just manually chunk for a 2-column grid to match the image
    Column {
        val chunks = timeSlots.chunked(2)
        chunks.forEach { rowSlots ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowSlots.forEachIndexed { index, slot ->
                    TimeSlotItem(
                        slot = slot,
                        isSelected = slot.time == selectedTime,
                        onSelect = { onTimeSelected(slot.time) },
                        modifier = Modifier.weight(1f)
                    )
                    if (index == 0 && rowSlots.size > 1) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
                if (rowSlots.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TimeSlotItem(
    slot: TimeSlot,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00ACC1) else Color(0xFFEEEEEE)),
        color = if (isSelected) Color(0xFFE0F7FA) else Color.White,
        modifier = modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = slot.time,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF00ACC1) else Color(0xFF0D1B2A),
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookSessionScreenPreview() {
    GenecareTheme {
        BookSessionScreen(NavController(androidx.compose.ui.platform.LocalContext.current))
    }
}

@Composable
fun UploadReportSection(
    fileName: String?,
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    if (fileName != null) {
        // File Selected State
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            border = BorderStroke(1.dp, Color(0xFF80CBC4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color(0xFF009688)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = fileName,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00695C),
                        maxLines = 1,
                    )
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    } else {
        // Upload Button State
        OutlinedButton(
            onClick = { launcher.launch("*/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF00ACC1)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00ACC1))
        ) {
            Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Report", fontWeight = FontWeight.SemiBold)
        }
    }
}
