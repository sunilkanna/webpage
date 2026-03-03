package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController, 
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val adminStats by viewModel.adminStats.collectAsState()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { AdminDashboardHeader(navController, onLogout = onLogout, stats = adminStats) }
            item { QuickActions(navController, stats = adminStats) }
            item { RecentActivity() }
            item { PlatformHealth(stats = adminStats) }
        }
    }
}

@Composable
fun AdminDashboardHeader(navController: NavController, onLogout: () -> Unit, stats: com.simats.genecare.data.model.AdminStatsResponse?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF8E24AA))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { navController.navigate("notifications") }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }

                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                }
            }
        }
        Text(
            text = "System Administration Panel",
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                DashboardStatCard(
                    "Pending Verifications", 
                    stats?.pendingVerifications?.toString() ?: "0", 
                    Icons.Default.History,
                    onClick = { navController.navigate("counselor_verification") }
                ) 
            }
            item { 
                DashboardStatCard(
                    "Active Counselors", 
                    stats?.activeCounselors?.toString() ?: "0", 
                    Icons.Default.VerifiedUser,
                    onClick = { navController.navigate("counselor_verification") }
                ) 
            }
            item { 
                DashboardStatCard(
                    "Total Patients", 
                    stats?.totalPatients?.toString() ?: "0", 
                    Icons.Default.Group,
                    onClick = { navController.navigate("user_management") } 
                ) 
            }
            item { 
                DashboardStatCard(
                    "System Alerts", 
                    stats?.systemAlerts?.toString() ?: "0", 
                    Icons.Default.Warning,
                    onClick = { navController.navigate("notifications") }
                ) 
            }
        }
    }
}

@Composable
fun DashboardStatCard(title: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(32.dp))
            Column {
                 Text(text = title, color = Color.White, fontSize = 14.sp)
                 Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickActions(navController: NavController, stats: com.simats.genecare.data.model.AdminStatsResponse?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(350.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { QuickActionCard("Verify Counsel", "${stats?.pendingVerifications ?: 0} pending", Icons.Default.CheckCircleOutline, onClick = { navController.navigate("counselor_verification") }) }
            item { QuickActionCard("Analytics", "Platform metrics", Icons.Default.BarChart, onClick = { navController.navigate("analytics") }) }
            item { QuickActionCard("User Management", "Manage users", Icons.Default.Group, onClick = { navController.navigate("user_management") }) }
            item { QuickActionCard("Reports & Logs", "Access system reports", Icons.Default.Report, onClick = { navController.navigate("reports_and_logs") }) }
            item { QuickActionCard("Notifications", "Send announcements", Icons.Default.Notifications, onClick = { navController.navigate("notifications") }) }
            item { QuickActionCard("System Settings", "Manage platform", Icons.Default.Settings, onClick = { navController.navigate("system_settings") }) }

        }
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, onClick: (() -> Unit)? = null) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp).height(100.dp).fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween) {
             Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RecentActivity() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ActivityItem("Dr. Sarah Jefferson verified", "10 mins ago", Icons.Default.CheckCircleOutline, Color(0xFF4CAF50))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ActivityItem("Server usage at 85%", "1 hour ago", Icons.Default.Warning, Color(0xFFF44336))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ActivityItem("25 new patient registrations", "2 hours ago", Icons.Default.Group, MaterialTheme.colorScheme.primary)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ActivityItem("System backup completed", "5 hours ago", Icons.Default.History, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ActivityItem(text: String, time: String, icon: ImageVector, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = text, fontWeight = FontWeight.Medium)
            Text(text = time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PlatformHealth(stats: com.simats.genecare.data.model.AdminStatsResponse?) {
    val alerts = stats?.systemAlerts ?: 0
    val healthStatus = if (alerts > 0) "Needs Attention" else "Healthy"
    val healthColor = if (alerts > 0) Color(0xFFF44336) else Color(0xFF4CAF50)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Platform Health", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp)) {
                HealthItem("Server Status", "Online", Color(0xFF4CAF50))
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                HealthItem("Database", healthStatus, healthColor)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                HealthItem("API Response", "45ms", Color(0xFF4CAF50))
                if (alerts > 0) {
                     Divider(modifier = Modifier.padding(vertical = 8.dp))
                     HealthItem("Active Alerts", "$alerts", Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun HealthItem(item: String, status: String, statusColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = item, fontWeight = FontWeight.Medium)
        Text(text = status, color = statusColor, fontWeight = FontWeight.Bold)
    }
}


@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    GenecareTheme {
        AdminDashboardScreen(rememberNavController(), onLogout = {})
    }
}
