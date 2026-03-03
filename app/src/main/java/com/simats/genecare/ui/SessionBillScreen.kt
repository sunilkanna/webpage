package com.simats.genecare.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme


import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.simats.genecare.RazorpayCallbackObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionBillScreen(navController: NavController, appointmentId: Int = 1) {
    val viewModel: SessionBillViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val appointmentDetails by viewModel.appointmentDetails.collectAsState()
    val billDetails by viewModel.billDetails.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val checkoutData by viewModel.razorpayCheckoutData.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(appointmentId) {
        val userType = com.simats.genecare.data.UserSession.getUserType()
        if (userType != "Patient") {
            navController.navigate("counselor_dashboard") {
                popUpTo("session_bill/$appointmentId") { inclusive = true }
            }
        } else {
            viewModel.loadBill(appointmentId)
        }
    }

    LaunchedEffect(checkoutData) {
        checkoutData?.let { data ->
            try {
                val checkout = com.razorpay.Checkout()
                checkout.setKeyID(data.keyId)

                val options = org.json.JSONObject()
                options.put("name", "Genecare")
                options.put("description", "Consultation Payment")
                options.put("order_id", data.orderId)
                options.put("currency", "INR")
                options.put("amount", data.amount) // Amount is already in paise
                options.put("theme.color", "#00838F")

                RazorpayCallbackObject.onSuccess = { paymentData ->
                    if (paymentData != null) {
                        viewModel.verifyPayment(
                            paymentId = data.internalPaymentId,
                            orderId = data.orderId,
                            razorpayPaymentId = paymentData.paymentId,
                            signature = paymentData.signature ?: "",
                            onSuccess = { navController.navigate("payment_success/${data.internalPaymentId}") }
                        )
                    }
                }
                RazorpayCallbackObject.onError = { _, _ ->
                    viewModel.setPaymentError("Payment was cancelled or failed")
                }

                activity?.let {
                    checkout.open(it, options)
                } ?: run {
                    viewModel.setPaymentError("Unable to open payment gateway")
                }
            } catch (e: Exception) {
                viewModel.setPaymentError(e.message ?: "Failed to initialize payment")
            } finally {
                viewModel.clearCheckoutData()
            }
        }
    }

    var selectedPaymentMethod by remember { mutableStateOf("UPI") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (paymentState.startsWith("Error")) {
                    Text(
                        text = paymentState,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Button(
                    onClick = { 
                        viewModel.processPayment(appointmentId, selectedPaymentMethod)
                    },
                    enabled = paymentState != "Processing" && paymentState != "Verifying",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)) // Teal color
                ) {
                    if (paymentState == "Processing" || paymentState == "Verifying") {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (paymentState == "Processing") "Initializing..." else "Verifying...")
                    } else {
                        val amount = billDetails?.totalAmount ?: 0.0
                        Text(
                            text = "Pay ₹${String.format("%.2f", amount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },

        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Session Bill",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Text(
                text = "Complete payment to finish your consultation",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Doctor Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                           Text(
                               text = appointmentDetails?.counselorName?.take(2)?.uppercase() ?: "DR", 
                               fontSize = 18.sp, 
                               fontWeight = FontWeight.Bold, 
                               color = Color(0xFF00ACC1)
                           )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = appointmentDetails?.counselorName ?: "Doctor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            Text(
                                text = "Video Consultation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Date", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(appointmentDetails?.appointmentDate ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF37474F))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                         Column {
                            Text("Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(appointmentDetails?.timeSlot ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF37474F))
                        }
                    }
                     Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Duration", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("45 minutes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF37474F))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bill Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bill Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (billDetails != null) {
                        BillRow("Consultation Fee", "₹${String.format("%.2f", billDetails!!.consultationFee)}")
                        BillRow("Platform Fee", "₹${String.format("%.2f", billDetails!!.platformFee)}")
                        BillRow("GST (18%)", "₹${String.format("%.2f", billDetails!!.gst)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                            Text("₹${String.format("%.2f", billDetails!!.totalAmount)}", fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1), fontSize = 18.sp)
                        }
                    } else {
                        // Loading State
                         CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
            Spacer(modifier = Modifier.height(12.dp))
            
            PaymentMethodParams(
                title = "UPI",
                subtitle = "Google Pay, PhonePe, Paytm",
                selected = selectedPaymentMethod == "UPI",
                onClick = { selectedPaymentMethod = "UPI" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaymentMethodParams(
                title = "Credit/Debit Card",
                subtitle = "Visa, Mastercard, Rupay",
                selected = selectedPaymentMethod == "Card",
                onClick = { selectedPaymentMethod = "Card" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaymentMethodParams(
                title = "Net Banking",
                subtitle = "All major banks",
                selected = selectedPaymentMethod == "NetBanking",
                onClick = { selectedPaymentMethod = "NetBanking" }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "By proceeding with payment, you agree to our Terms & Conditions and Refund Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF57F17),
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
        }
    }
}

@Composable
fun BillRow(label: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Text(amount, color = Color(0xFF37474F), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PaymentMethodParams(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFE0F7FA).copy(alpha = 0.3f) else Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Color(0xFF00ACC1) else Color(0xFFEEEEEE),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEEEEEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
               // Icon
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF37474F))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00ACC1))
            )
        }
    }
}

@Preview
@Composable
fun SessionBillScreenPreview() {
    GenecareTheme {
        SessionBillScreen(rememberNavController())
    }
}
