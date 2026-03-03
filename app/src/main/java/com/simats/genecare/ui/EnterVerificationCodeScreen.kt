package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterVerificationCodeScreen(
    navController: NavController,
    email: String,
    viewModel: ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var code by remember { mutableStateOf(List(5) { "" }) }
    var countdown by remember { mutableStateOf(60) }
    val focusRequesters = remember { List(5) { FocusRequester() } }
    val focusManager = LocalFocusManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    val verifyOtpState by viewModel.verifyOtpState.collectAsState()
    val sendOtpState by viewModel.sendOtpState.collectAsState()

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    LaunchedEffect(verifyOtpState) {
        when (verifyOtpState) {
            is OtpState.Success -> {
                android.widget.Toast.makeText(context, (verifyOtpState as OtpState.Success).message, android.widget.Toast.LENGTH_SHORT).show()
                navController.navigate("create_new_password/$email")
                viewModel.resetVerifyOtpState()
            }
            is OtpState.Error -> {
                android.widget.Toast.makeText(context, (verifyOtpState as OtpState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetVerifyOtpState()
            }
            else -> {}
        }
    }

    // Handle resend OTP success
    LaunchedEffect(sendOtpState) {
        when (sendOtpState) {
            is OtpState.Success -> {
                android.widget.Toast.makeText(context, "New code sent!", android.widget.Toast.LENGTH_SHORT).show()
                countdown = 60
                viewModel.resetSendOtpState()
            }
            is OtpState.Error -> {
                android.widget.Toast.makeText(context, (sendOtpState as OtpState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetSendOtpState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We've sent a 5-digit code to\n$email",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in 0..4) {
                    OutlinedTextField(
                        value = code[i],
                        onValueChange = { value ->
                            if (value.length > 1) { // Handle paste
                                if (value.length == 5) {
                                    code = value.map { it.toString() }
                                    focusManager.clearFocus()
                                }
                            } else {
                                val currentVal = code[i]
                                val newCode = code.toMutableList()
                                newCode[i] = value
                                code = newCode

                                if (value.isEmpty() && currentVal.isNotEmpty()) {
                                    // Character deleted, do nothing with focus
                                } else if (value.isEmpty()) { // Backspace on empty field
                                    if (i > 0) {
                                        focusRequesters[i - 1].requestFocus()
                                    }
                                } else { // Character entered
                                    if (i < 4) {
                                        focusRequesters[i + 1].requestFocus()
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .focusRequester(focusRequesters[i]),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = VisualTransformation.None,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Resend code
            if (countdown > 0) {
                Text("Resend code in ${countdown}s", color = MaterialTheme.colorScheme.primary)
            } else {
                TextButton(
                    onClick = {
                        viewModel.sendOtp(email)
                    },
                    enabled = sendOtpState !is OtpState.Loading
                ) {
                    Text("Resend Code", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (verifyOtpState is OtpState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val enteredOtp = code.joinToString("")
                        if (enteredOtp.length == 5) {
                            viewModel.verifyOtp(email, enteredOtp)
                        } else {
                            android.widget.Toast.makeText(context, "Please enter all 5 digits", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = code.joinToString("").length == 5
                ) {
                    Text("Verify Code", fontSize = 16.sp)
                }
            }
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Change Email Address")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnterVerificationCodeScreenPreview() {
    GenecareTheme {
        EnterVerificationCodeScreen(rememberNavController(), "kannasuneel04@gmail.com")
    }
}
