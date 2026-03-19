package com.simats.genecare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsScreen(navController: NavController) {
    val viewModel: SystemSettingsViewModel = viewModel()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (isLoading && settings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // General Settings Section
                SettingsSectionHeader("General")
                SettingSwitchItem(
                    title = "Maintenance Mode",
                    subtitle = "Only admins can access the platform",
                    icon = Icons.Default.PowerSettingsNew,
                    checked = settings["maintenance_mode"] == "1",
                    onCheckedChange = { viewModel.updateSetting("maintenance_mode", it) }
                )
                
                // GST Percentage Setting
                var gstText by remember(settings["gst_percentage"]) { 
                    mutableStateOf(settings["gst_percentage"] ?: "5") 
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "GST Percentage (%)", fontWeight = FontWeight.Medium)
                        Text(text = "Adjust the tax percentage for billing", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    OutlinedTextField(
                        value = gstText,
                        onValueChange = { 
                            gstText = it
                            if (it.isNotEmpty()) viewModel.updateStringSetting("gst_percentage", it)
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Notifications Section
                SettingsSectionHeader("Notifications")
                SettingSwitchItem(
                    title = "Email Alerts",
                    subtitle = "Receive system alerts via email",
                    icon = Icons.Default.Notifications,
                    checked = settings["email_alerts"] == "1",
                    onCheckedChange = { viewModel.updateSetting("email_alerts", it) }
                )
                SettingSwitchItem(
                    title = "Push Notifications",
                    subtitle = "Receive updates on mobile devices",
                    icon = Icons.Default.Notifications,
                    checked = settings["push_notifications"] == "1",
                    onCheckedChange = { viewModel.updateSetting("push_notifications", it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Appearance Section
                SettingsSectionHeader("Appearance")
                SettingSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Enable dark theme for admin panel",
                    icon = Icons.Default.Palette,
                    checked = settings["dark_mode"] == "1",
                    onCheckedChange = { viewModel.updateSetting("dark_mode", it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Security Section
                SettingsSectionHeader("Security")
                SettingSwitchItem(
                    title = "Two-Factor Authentication",
                    subtitle = "Require 2FA for all admin logins",
                    icon = Icons.Default.Security,
                    checked = settings["two_factor_auth"] == "1",
                    onCheckedChange = { viewModel.updateSetting("two_factor_auth", it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Version Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("GeneCare Platform", fontWeight = FontWeight.Bold)
                            Text("Version 1.0.0 (Build 20260209)", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SystemSettingsScreenPreview() {
    GenecareTheme {
        SystemSettingsScreen(rememberNavController())
    }
}
