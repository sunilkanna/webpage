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
import androidx.compose.material.icons.automirrored.filled.Logout
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val adminStats by viewModel.adminStats.collectAsState()


    androidx.activity.compose.BackHandler {
        (context as? android.app.Activity)?.finish()
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { AdminDashboardHeader(navController, onLogout = onLogout, stats = adminStats) }
            item { QuickActions(navController, stats = adminStats) }
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
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
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
            modifier = Modifier.height(110.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                DashboardStatCard(
                    "Active Counselors", 
                    stats?.activeCounselors?.toString() ?: "0", 
                    Icons.Default.VerifiedUser,
                    onClick = { navController.navigate("user_management/Counselor") }
                ) 
            }
            item { 
                DashboardStatCard(
                    "Total Patients", 
                    stats?.totalPatients?.toString() ?: "0", 
                    Icons.Default.Group,
                    onClick = { navController.navigate("user_management/Patient") } 
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
fun QuickActions(
navController: NavController, stats: com.simats.genecare.data.model.AdminStatsResponse?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val actions = listOf(
                Triple("Verify Counsel", "${stats?.pendingVerifications ?: 0} pending", Icons.Default.CheckCircleOutline to "counselor_verification"),
                Triple("Analytics", "Platform metrics", Icons.Default.BarChart to "analytics"),
                Triple("User Management", "Manage users", Icons.Default.Group to "user_management/all"),
                Triple("Reports & Logs", "Access system reports", Icons.Default.Report to "reports_and_logs"),
                Triple("Notifications", "Send announcements", Icons.Default.Notifications to "notifications"),
                Triple("System Settings", "Manage platform", Icons.Default.Settings to "system_settings")
            )

            for (i in actions.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        title = actions[i].first,
                        subtitle = actions[i].second,
                        icon = actions[i].third.first,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(actions[i].third.second) }
                    )
                    if (i + 1 < actions.size) {
                        QuickActionCard(
                            title = actions[i + 1].first,
                            subtitle = actions[i + 1].second,
                            icon = actions[i + 1].third.first,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(actions[i + 1].third.second) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String, 
    subtitle: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(modifier = Modifier.padding(16.dp).height(100.dp).fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween) {
             Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    GenecareTheme {
        AdminDashboardScreen(rememberNavController(), onLogout = {})
    }
}
