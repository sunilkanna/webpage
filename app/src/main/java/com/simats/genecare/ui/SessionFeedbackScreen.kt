package com.simats.genecare.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionFeedbackScreen(navController: NavController, appointmentId: Int = 1) {
    val viewModel: SessionBillViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val appointmentDetails by viewModel.appointmentDetails.collectAsState()

    androidx.compose.runtime.LaunchedEffect(appointmentId) {
        viewModel.loadBill(appointmentId)
    }

    var rating by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }

    val feedbackState by viewModel.feedbackState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(feedbackState) {
        if (feedbackState == "Success") {
            android.widget.Toast.makeText(context, "Feedback submitted successfully!", android.widget.Toast.LENGTH_SHORT).show()
            navController.navigate("dashboard") {
                popUpTo("dashboard") { inclusive = true }
            }
        } else if (feedbackState.startsWith("Error")) {
            android.widget.Toast.makeText(context, feedbackState, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Feedback", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "How was your session?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Doctor Profile
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F7FA)),
                contentAlignment = Alignment.Center
            ) {
                 Text(
                     text = appointmentDetails?.counselorName?.take(2)?.uppercase() ?: "DR", 
                     fontSize = 32.sp, 
                     fontWeight = FontWeight.Bold, 
                     color = Color(0xFF00ACC1)
                 )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = appointmentDetails?.counselorName ?: "Doctor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
             Text(
                text = "Genomic Counselor",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Star Rating
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { rating = i },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= rating) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Feedback TextField
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Write your review (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00ACC1),
                    unfocusedBorderColor = Color(0xFFEEEEEE)
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (feedbackState == "Submitting") {
                CircularProgressIndicator(color = Color(0xFF00838F))
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { 
                     viewModel.submitFeedback(appointmentId, rating, feedbackText)
                },
                enabled = rating > 0 && feedbackState != "Submitting",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00838F),
                    disabledContainerColor = Color(0xFFB2EBF2)
                )
            ) {
                Text(
                    text = "Submit Feedback",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { 
                    navController.navigate("dashboard") {
                         popUpTo("dashboard") { inclusive = true }
                     }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
fun SessionFeedbackScreenPreview() {
    GenecareTheme {
        SessionFeedbackScreen(rememberNavController())
    }
}
