package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme
import com.simats.genecare.data.UserSession

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import androidx.activity.compose.BackHandler

@Composable
fun CounselorDashboardScreen(
    navController: NavController, 
    onSignOut: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val user = UserSession.getUser()

    BackHandler {
        (context as? Activity)?.finish()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FE) // Soft off-white background
    ) { padding ->
        when (val state = uiState) {
            is DashboardState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF5C6BC0))
                }
            }
            is DashboardState.Success -> {
                val stats = state.stats.counselorStats
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Header and Stats Section
                    item {
                        TopSection(onSignOut, navController, user?.fullName ?: "Counselor", stats)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Today's Schedule
                    item {
                        TodaysSchedule(
                            appointments = stats?.todayAppointments ?: emptyList(),
                            onViewAllClick = { navController.navigate("counselor_appointments") },
                            onStartClick = { appointmentId -> navController.navigate("video-call/$appointmentId") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Quick Actions
                    item {
                        QuickActionsSection(navController, stats?.pendingRequestsCount ?: 0)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    
                    // Recent Reviews
                    item {
                        RecentReviewsSection(stats?.recentReviews ?: emptyList())
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            is DashboardState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.clickable { viewModel.fetchStats() })
                }
            }
        }
    }
}

@Composable
fun TopSection(onSignOut: () -> Unit, navController: NavController, name: String, stats: com.simats.genecare.data.model.CounselorDashboardStats?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep Indigo
                        Color(0xFF303F9F)  // Rich Indigo
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Back/SignOut Button
                 IconButton(
                    onClick = onSignOut,
                     modifier = Modifier
                         .background(Color.White.copy(alpha = 0.15f), CircleShape)
                         .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Notification Button
                IconButton(
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .size(40.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .background(Color(0xFFFF6B6B), CircleShape) // Soft red dot
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile info
            Text(
                text = "Dr. $name",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                )
            )
            Text(
                text = "Genetic Counselor",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB3B9E6), // Soft lavender
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    icon = Icons.Outlined.Videocam,
                    label = "Today's Sessions",
                    value = "${stats?.todaysSessions ?: 0}",
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    valueColor = Color(0xFFFFD54F), // Warm amber/gold
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Outlined.Groups,
                    label = "Total Patients",
                    value = "${stats?.totalPatients ?: 0}",
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White,
                    valueColor = Color(0xFF4DD0E1), // Bright cyan
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color, 
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp), 
                    color = contentColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}


@Composable
fun TodaysSchedule(
    appointments: List<com.simats.genecare.data.model.AppointmentData>,
    onViewAllClick: () -> Unit, 
    onStartClick: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E) // Deep indigo heading
            )
            TextButton(onClick = onViewAllClick) {
                Text("View All", color = Color(0xFF5C6BC0), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (appointments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text(text = "No appointments today", color = Color(0xFF9E9E9E))
            }
        } else {
            appointments.forEach { appointment ->
                ScheduleItem(
                    name = appointment.patientName ?: "Patient",
                    detail = "Video Consultation",
                    time = appointment.timeSlot,
                    imageInitial = (appointment.patientName ?: "P").take(1),
                    imageColor = Color(0xFFC5CAE9), // Soft indigo background
                    onStartClick = { onStartClick(appointment.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ScheduleItem(name: String, detail: String, time: String, imageInitial: String, imageColor: Color, onStartClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(imageColor),
                contentAlignment = Alignment.Center
            ) {
                 Text("👱‍♀️", fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF1A237E))
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color(0xFF5C6BC0), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(time, color = Color(0xFF5C6BC0), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Start", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController, pendingCount: Int = 0) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E) // Deep indigo heading
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Outlined.DateRange, 
                label = "Session\nRequests", 
                bgColor = Color(0xFFE8EAF6), // Light indigo
                iconColor = Color(0xFF3F51B5), // Indigo
                modifier = Modifier.weight(1f),
                badgeCount = pendingCount,
                onClick = { navController.navigate("session_requests") }
            )
            QuickActionCard(
                icon = Icons.Outlined.Groups, 
                label = "Patient\nList", 
                bgColor = Color(0xFFE0F7FA), // Light cyan
                iconColor = Color(0xFF00ACC1), // Cyan
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("patient_list") }
            )
            QuickActionCard(
                icon = Icons.Outlined.ChatBubbleOutline, 
                label = "Messages", 
                bgColor = Color(0xFFFCE4EC), // Light pink
                iconColor = Color(0xFFE91E63), // Pink
                modifier = Modifier.weight(1f), 
                onClick = { navController.navigate("counselor_messages") }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                icon = Icons.Outlined.PieChart, 
                label = "Patient\nReports", 
                bgColor = Color(0xFFF3E5F5), // Light purple
                iconColor = Color(0xFF9C27B0), // Purple
                modifier = Modifier.weight(1f), 
                onClick = { navController.navigate("counselor_reports") }
            )
            @Suppress("DEPRECATION")
            QuickActionCard(
                icon = Icons.Outlined.TrendingUp, 
                label = "Performance", 
                bgColor = Color(0xFFE8F5E9), // Light green
                iconColor = Color(0xFF43A047), // Green
                modifier = Modifier.weight(1f), 
                onClick = { navController.navigate("counselor_performance") }
            )
            QuickActionCard(
                icon = Icons.Outlined.Settings, 
                label = "Settings", 
                bgColor = Color(0xFFF5F5F5), // Light grey
                iconColor = Color(0xFF616161), // Dark grey
                modifier = Modifier.weight(1f), 
                onClick = { navController.navigate("counselor_settings") }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector, 
    label: String, 
    bgColor: Color, 
    iconColor: Color, 
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF616161), // Darker grey for better readability
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            if (badgeCount > 0) {
                Surface(
                    color = Color(0xFFFF6B6B), // Soft red badge
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RecentReviewsSection(reviews: List<com.simats.genecare.data.model.ReviewData>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recent Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (reviews.isEmpty()) {
            Text("No reviews yet.", color = Color(0xFF9E9E9E), style = MaterialTheme.typography.bodyMedium)
        } else {
            reviews.forEach { review ->
                ReviewCard(
                    rating = review.rating,
                    daysAgo = review.daysAgo,
                    review = "\"${review.review}\"",
                    author = "- ${review.author}"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(rating: Int, daysAgo: String, review: String, author: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    repeat(rating) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    }
                }
                Text(daysAgo, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(review, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
            Spacer(modifier = Modifier.height(4.dp))
            Text(author, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CounselorDashboardScreenPreview() {
    GenecareTheme {
        CounselorDashboardScreen(rememberNavController())
    }
}
