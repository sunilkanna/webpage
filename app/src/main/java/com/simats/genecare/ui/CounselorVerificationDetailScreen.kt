package com.simats.genecare.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
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
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounselorVerificationDetailScreen(navController: NavController, viewModel: CounselorViewModel) {
    val selectedCounselor by viewModel.selectedCounselor.collectAsState()

    if (selectedCounselor == null) {
        // Fallback if no counselor selected
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No counselor selected")
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
        return
    }

    val counselor = selectedCounselor!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Counselor Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Access Warning
            AdminAccessWarning()

            // Counselor Profile Card
            CounselorProfileCard(counselor)

            // Uploaded Certificate
            // Uploaded Certificate
            UploadedCertificateCard(certificateUrl = counselor.certificateUrl)

            // Submission Details
            SubmissionDetailsCard(counselor)

            // Consultation Fee
            ConsultationFeeCard(counselor) { newFee ->
                viewModel.updateFee(newFee)
            }

            // Rejection Reason Input
            var rejectionReason by remember { mutableStateOf("") }
            var showRejectionError by remember { mutableStateOf(false) }
            
            if (counselor.status == "Rejected" && counselor.rejectionReason != null) {
                 Text(
                    text = "Rejection Reason: ${counselor.rejectionReason}",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (counselor.status == "Pending") {
                 RejectionReasonInputField(rejectionReason, showRejectionError) { 
                     rejectionReason = it 
                     showRejectionError = false
                 }
            }

            // Action Buttons
            ActionButtons(navController, viewModel, counselor, rejectionReason) { showRejectionError = true }
        }
    }
}

@Composable
fun AdminAccessWarning() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)), // Light Purple
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = "Admin Access",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(18.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Admin Access: You have privileged access to view and verify counselor credentials. Handle all documents with confidentiality.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6A1B9A),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun CounselorProfileCard(counselor: CounselorViewModel.Counselor) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00ACC1)), // Cyan/Teal
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = counselor.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = counselor.email,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Medical Council", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(counselor.medicalCouncil, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Reg. Year", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(counselor.registrationYear, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Reg. Number", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(counselor.registrationNumber, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun UploadedCertificateCard(certificateUrl: String) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Uploaded Certificate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // File Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Icon(
                        Icons.Default.Description,
                        contentDescription = "PDF",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("genetic_counseling_cert.pdf", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("2.4 MB", fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    try {
                        if (certificateUrl.isNotEmpty()) {
                            if (certificateUrl.contains("example.com")) {
                                android.widget.Toast.makeText(context, "This record uses a dummy certificate. Please ask the counselor to re-submit.", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(certificateUrl))
                                context.startActivity(intent)
                            }
                        } else {
                            android.widget.Toast.makeText(context, "No certificate URL available", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Error opening document: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)), // Teal darken
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Full Document", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SubmissionDetailsCard(counselor: CounselorViewModel.Counselor) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Submission Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Submitted on:", color = Color.Gray, fontSize = 14.sp)
                Text(counselor.submitted, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status:", color = Color.Gray, fontSize = 14.sp)
                
                val statusColor = when (counselor.status) {
                    "Approved" -> Color(0xFF4CAF50)
                    "Rejected" -> Color.Red
                    else -> Color(0xFFFFA000)
                }
                val bgColor = when (counselor.status) {
                    "Approved" -> Color(0xFFE8F5E9)
                    "Rejected" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFFFF8E1)
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                     Text(counselor.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ConsultationFeeCard(counselor: CounselorViewModel.Counselor, onUpdateFee: (String) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditFeeDialog(
            currentFee = counselor.fee.takeWhile { it.isDigit() },
            onDismiss = { showEditDialog = false },
            onConfirm = { newFee ->
                onUpdateFee(newFee)
                showEditDialog = false
            }
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Light Green
        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consultation Fee", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.background(Color.White)) {
                     Row(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = counselor.fee.takeWhile { it.isDigit() },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "INR",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = "Per 60-minute session",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditFeeDialog(
    currentFee: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var fee by remember { mutableStateOf(currentFee) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Consultation Fee") },
        text = {
            OutlinedTextField(
                value = fee,
                onValueChange = { if (it.all { char -> char.isDigit() }) fee = it },
                label = { Text("Fee (INR)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { if (fee.isNotBlank()) onConfirm(fee) }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RejectionReasonInputField(reason: String, showRejectionError: Boolean, onReasonChange: (String) -> Unit) {
    Column {
        Text(
            text = "Rejection Reason * (Required if rejecting)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = reason,
            onValueChange = onReasonChange,
            placeholder = { Text("Provide a clear reason for rejection...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            isError = showRejectionError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedContainerColor = Color.White
            )
        )
        if (showRejectionError) {
             Text(
                text = "Reason is required for rejection",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ActionButtons(
    navController: NavController, 
    viewModel: CounselorViewModel,
    counselor: CounselorViewModel.Counselor,
    rejectionReason: String,
    onShowRejectionError: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (counselor.status == "Pending") {
            Button(
                onClick = { 
                    viewModel.approveCounselor()
                    navController.navigate("admin_dashboard") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)), // Green
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Approve Counselor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = { 
                    if (rejectionReason.isBlank()) {
                        onShowRejectionError()
                    } else {
                        viewModel.rejectCounselor(rejectionReason)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)), // Red
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reject Application", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Back to List", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CounselorVerificationDetailScreenPreview() {
    GenecareTheme {
        CounselorVerificationDetailScreen(rememberNavController(), CounselorViewModel())
    }
}
