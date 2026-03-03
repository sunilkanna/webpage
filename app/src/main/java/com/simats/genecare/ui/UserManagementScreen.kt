package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.genecare.data.network.ApiClient
import com.simats.genecare.data.model.UserData
import com.simats.genecare.data.model.ManageUserRequest
import kotlinx.coroutines.launch
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val users = remember { mutableStateListOf<UserData>() }
    
    val snackbarHostState = remember { SnackbarHostState() }
    var userToDelete by remember { mutableStateOf<UserData?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = ApiClient.api.getUsers()
            if (response.isSuccessful && response.body() != null) {
                users.clear()
                users.addAll(response.body()!!.users)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${userToDelete?.name}? This action will permanently remove all their associated data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val user = userToDelete!!
                        showDeleteDialog = false
                        scope.launch {
                            try {
                                val res = ApiClient.api.manageUser(ManageUserRequest(user.id.toInt(), "delete"))
                                if (res.isSuccessful) {
                                    users.remove(user)
                                    snackbarHostState.showSnackbar("User deleted successfully")
                                } else {
                                    snackbarHostState.showSnackbar("Error: ${res.message()}")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to delete user")
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // User List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || 
                        it.email.contains(searchQuery, ignoreCase = true) 
                    }) { user ->
                        UserCard(user = user, onDelete = { 
                            userToDelete = user
                            showDeleteDialog = true
                        }, onSuspend = { 
                            // TODO: Add suspension logic if backend supports it
                            scope.launch {
                                snackbarHostState.showSnackbar("Suspension feature coming soon")
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserData, onDelete: () -> Unit, onSuspend: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (user.role == "Counselor") Color(0xFFE0F2F1) else Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.first().toString(),
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == "Counselor") Color(0xFF00695C) else Color(0xFF1565C0),
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = user.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (user.role == "Counselor") Color(0xFFE0F2F1) else Color(0xFFE3F2FD),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = user.role,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (user.role == "Counselor") Color(0xFF00695C) else Color(0xFF1565C0)
                            )
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (user.status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = user.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (user.status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // Actions
            Row {
                IconButton(onClick = onSuspend) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Suspend",
                        tint = if (user.status == "Active") Color(0xFFFF9800) else Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UserManagementScreenPreview() {
    GenecareTheme {
        UserManagementScreen(rememberNavController())
    }
}
