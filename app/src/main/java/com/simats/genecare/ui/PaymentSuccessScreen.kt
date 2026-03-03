package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme

@Composable
fun PaymentSuccessScreen(navController: NavController, appointmentId: Int = 1) {
    val viewModel: SessionBillViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val appointmentDetails by viewModel.appointmentDetails.collectAsState()
    val billDetails by viewModel.billDetails.collectAsState()

    androidx.compose.runtime.LaunchedEffect(appointmentId) {
        viewModel.loadBill(appointmentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFE0F7FA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color(0xFF00ACC1),
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        
        Text(
            text = "Your consultation with ${appointmentDetails?.counselorName ?: "your counselor"} has been paid successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Session Date", color = Color.Gray, fontSize = 14.sp)
                    Text(appointmentDetails?.appointmentDate ?: "N/A", fontWeight = FontWeight.Medium, color = Color(0xFF37474F), fontSize = 14.sp)
                }
                 Spacer(modifier = Modifier.height(12.dp))
                 Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Amount Paid", color = Color.Gray, fontSize = 14.sp)
                    Text("₹${String.format("%.2f", billDetails?.totalAmount ?: 0.0)}", fontWeight = FontWeight.Bold, color = Color(0xFF37474F), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                  navController.navigate("session_feedback/$appointmentId") 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)) // Teal color
        ) {
            Text(
                text = "Give Feedback",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun PaymentSuccessScreenPreview() {
    GenecareTheme {
        PaymentSuccessScreen(rememberNavController())
    }
}
