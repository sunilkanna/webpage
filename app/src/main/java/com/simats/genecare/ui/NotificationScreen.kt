package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    VERIFICATION_PENDING, SYSTEM_ALERT, NEW_USER, VERIFICATION_COMPLETE, BACKUP, SESSION_REMINDER, MESSAGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController, viewModel: NotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Notifications", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(
                            "Mark all read",
                            color = Color(0xFF00ACC1), // Cyan/Teal color
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White 
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && notifications.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00ACC1)
                )
            } else if (error != null && notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(error!!, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
                    Button(
                        onClick = { viewModel.fetchNotifications() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            } else if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                    Text("We'll notify you when something important happens", color = Color.LightGray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)), // Very light grey/white background
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            NotificationType.VERIFICATION_PENDING -> Color(0xFFF3E5F5) // Light Purple
                            NotificationType.SYSTEM_ALERT -> Color(0xFFFFEBEE)         // Light Red
                            NotificationType.NEW_USER -> Color(0xFFE0F7FA)            // Light Cyan
                            NotificationType.VERIFICATION_COMPLETE -> Color(0xFFE8F5E9) // Light Green
                            NotificationType.BACKUP -> Color(0xFFE3F2FD)              // Light Blue
                            NotificationType.SESSION_REMINDER -> Color(0xFFFFF3E0)    // Light Orange
                            NotificationType.MESSAGE -> Color(0xFFF3E5F5)             // Light Purple
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.VERIFICATION_PENDING -> Icons.Outlined.Security
                        NotificationType.SYSTEM_ALERT -> Icons.Outlined.Warning
                        NotificationType.NEW_USER -> Icons.Outlined.Group
                        NotificationType.VERIFICATION_COMPLETE -> Icons.Outlined.CheckCircle
                        NotificationType.BACKUP -> Icons.Outlined.Backup
                        NotificationType.SESSION_REMINDER -> Icons.Outlined.Schedule
                        NotificationType.MESSAGE -> Icons.Outlined.ChatBubbleOutline
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.VERIFICATION_PENDING -> Color(0xFF9C27B0) // Purple
                        NotificationType.SYSTEM_ALERT -> Color(0xFFE53935)       // Red
                        NotificationType.NEW_USER -> Color(0xFF00ACC1)          // Cyan
                        NotificationType.VERIFICATION_COMPLETE -> Color(0xFF43A047) // Green
                        NotificationType.BACKUP -> Color(0xFF1E88E5)            // Blue
                        NotificationType.SESSION_REMINDER -> Color(0xFFFF9800)  // Orange
                        NotificationType.MESSAGE -> Color(0xFF9C27B0)           // Purple
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B2A),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF546E7A),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    GenecareTheme {
        NotificationScreen(rememberNavController())
    }
}
