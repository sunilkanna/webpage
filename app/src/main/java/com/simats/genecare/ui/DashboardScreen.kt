package com.simats.genecare.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.simats.genecare.data.UserSession
import com.simats.genecare.ui.theme.GenecareTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import android.app.Activity
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val user = UserSession.getUser()

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.fetchStats()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    BackHandler {
        (context as? Activity)?.finish()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA) // Light grey background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Section
            DashboardHeader(navController, context, user?.fullName ?: "User")

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is DashboardState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00ACC1))
                    }
                }
                is DashboardState.Success -> {
                    val stats = state.stats.patientStats
                    
                    // Upcoming Appointment Card
                    AppointmentCard(navController, stats?.upcomingAppointment, bookingViewModel)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick Actions
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B2A)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    QuickActionsGrid(
                        navController = navController,
                        counselorId = stats?.upcomingAppointment?.counselorId,
                        counselorName = stats?.upcomingAppointment?.counselorName
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Health Insights
                    Text(
                        text = "Health Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B2A)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HealthInsightsCard(
                        score = stats?.riskScore ?: 0,
                        category = stats?.riskCategory ?: "Not Assessed",
                        lastUpdate = stats?.lastAssessmentDate ?: "Never",
                        onClick = { /* TODO */ }
                    )
                }
                is DashboardState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { viewModel.fetchStats() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = Color(0xFF00ACC1)
            )
        }
    }
}

@Composable
fun DashboardHeader(navController: NavController, context: Context, name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hello, $name",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B2A),
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7B8D9E)
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.navigate("notifications") },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF0D1B2A),
                        modifier = Modifier.padding(8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .background(Color(0xFFFF5252), CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            IconButton(
                onClick = { 
                    UserSession.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFCC3333),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(navController: NavController, appointment: com.simats.genecare.data.model.AppointmentData?, bookingViewModel: BookingViewModel? = null) {
    val isPending = appointment?.status == "Pending"
    val isConfirmed = appointment?.status == "Confirmed"
    
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (appointment != null) {
                    bookingViewModel?.setAppointmentId(appointment.id)
                    navController.navigate("appointment_details/${appointment.id}") 
                } else {
                    navController.navigate("appointment_details") 
                }
            }
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (appointment != null) {
                            if (isPending) listOf(Color(0xFFFFA726), Color(0xFFFFB74D)) // Orange for pending
                            else listOf(Color(0xFF00ACC1), Color(0xFF00C853)) // Green/Cyan for valid
                        } else {
                            listOf(Color(0xFFB0BEC5), Color(0xFF78909C))
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when {
                                appointment == null -> "No Upcoming Sessions"
                                isPending -> "Appointment Sent"
                                else -> "Upcoming Appointment"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (appointment != null) Color.White.copy(alpha = 0.9f) else Color(0xCC0D1B2A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appointment?.counselorName ?: "Book a Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (appointment != null) Color.White else Color(0xFF0D1B2A)
                        )
                        if (appointment != null) {
                            Text(
                                text = "Genetic Counselor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = if (appointment != null) Color.White else Color(0xFF78909C)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(color = Color(0x33FFFFFF))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appointment != null) "${appointment.appointmentDate} • ${appointment.timeSlot}" else "Ready to start your journey?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (appointment != null) Color.White.copy(alpha = 0.9f) else Color(0xCC0D1B2A)
                    )
                    
                    Button(
                        onClick = { 
                            if (appointment != null) {
                                bookingViewModel?.setAppointmentId(appointment.id)
                                navController.navigate("appointment_details/${appointment.id}")
                            }
                            else navController.navigate("book_session")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when {
                                appointment == null -> "Book Now"
                                isPending -> "Appointment Sent"
                                else -> "Join Now"
                            },
                            color = if (isPending) Color(0xFFEF6C00) else Color(0xFF00ACC1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    navController: NavController,
    counselorId: Int? = null,
    counselorName: String? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(
                icon = Icons.Outlined.CalendarToday,
                label = "Book Session",
                color = Color(0xFF00ACC1), // Cyan
                backgroundColor = Color(0xFFE0F7FA),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("book_session") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionItem(
                icon = Icons.Default.CheckCircle,
                label = "My Appointment",
                color = Color(0xFF00BFA5), // Teal
                backgroundColor = Color(0xFFE0F2F1),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("appointment_details") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = "Chat",
                color = Color(0xFFAA00FF), // Purple
                backgroundColor = Color(0xFFF3E5F5),
                modifier = Modifier.weight(1f),
                onClick = { 
                    if (counselorId != null && counselorName != null) {
                        navController.navigate("chat/$counselorId/$counselorName") 
                    } else {
                        // If no appointment, maybe take them to book session or show toast
                        // For now, let's just navigate to a general message or book session
                        navController.navigate("book_session")
                    }
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuickActionItem(
                icon = Icons.Default.Person,
                label = "Patient Profile",
                color = Color(0xFFFFA000), // Amber
                backgroundColor = Color(0xFFFFF8E1),
                modifier = Modifier.weight(1f),
                onClick = { 
                    val currentUserId = UserSession.getUserId() ?: 0
                    navController.navigate("patient_details/$currentUserId") 
                }
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(100.dp).clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B2A)
            )
        }
    }
}

@Composable
fun HealthInsightsCard(score: Int, category: String, lastUpdate: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Risk Assessment Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B2A)
                )
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (score < 30) Color(0xFF00C853) else if (score < 70) Color(0xFFFFA000) else Color.Red
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { score.toFloat() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (score < 30) Color(0xFF00C853) else if (score < 70) Color(0xFFFFA000) else Color.Red,
                trackColor = Color(0xFFEEEEEE),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Last updated: $lastUpdate",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7B8D9E)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    GenecareTheme {
        DashboardScreen(NavController(LocalContext.current))
    }
}
