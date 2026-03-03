package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.genecare.ui.theme.GenecareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewPasswordScreen(
    navController: NavController,
    email: String,
    viewModel: ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val resetPasswordState by viewModel.resetPasswordState.collectAsState()

    LaunchedEffect(resetPasswordState) {
        when (resetPasswordState) {
            is OtpState.Success -> {
                android.widget.Toast.makeText(context, (resetPasswordState as OtpState.Success).message, android.widget.Toast.LENGTH_SHORT).show()
                navController.navigate("password_reset_successful") {
                    popUpTo("forgot_password") { inclusive = true }
                }
                viewModel.resetResetPasswordState()
            }
            is OtpState.Error -> {
                android.widget.Toast.makeText(context, (resetPasswordState as OtpState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetResetPasswordState()
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
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Create New Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set a strong password for your account\n$email",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                placeholder = { Text("Enter new password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF008080)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray)
                    }
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF008080),
                    focusedLabelColor = Color(0xFF008080),
                    cursorColor = Color(0xFF008080)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordRequirements(password)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF008080)) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(if (confirmPasswordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray)
                    }
                },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF008080),
                    focusedLabelColor = Color(0xFF008080),
                    cursorColor = Color(0xFF008080)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Match Indicator
            if (password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(4.dp))
                         Text("Passwords match", color = Color(0xFF4CAF50), fontSize = 14.sp)
                    }
                } else {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                         Spacer(modifier = Modifier.width(4.dp))
                         Text("Passwords do not match", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val isPasswordValid = password.length >= 8 && 
                                  password.any { it.isUpperCase() } && 
                                  password.any { it.isLowerCase() } && 
                                  password.any { it.isDigit() } && 
                                  password.any { !it.isLetterOrDigit() }

            if (resetPasswordState is OtpState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.resetPassword(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008080),
                        disabledContainerColor = Color(0xFFB2DFDB)
                    ),
                    enabled = isPasswordValid && password == confirmPassword
                ) {
                    Text("Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PasswordRequirements(password: String) {
    val hasLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)) // Light Teal bg
            .padding(16.dp)
    ) {
        Text("Password must contain:", fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
        Spacer(modifier = Modifier.height(8.dp))
        RequirementItem("At least 8 characters", hasLength)
        RequirementItem("One uppercase letter (A-Z)", hasUpperCase)
        RequirementItem("One lowercase letter (a-z)", hasLowerCase)
        RequirementItem("One number (0-9)", hasNumber)
        RequirementItem("One special character (!@#\$%...)", hasSpecial)
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF009688) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) Color(0xFF004D40) else Color.Gray,
            fontWeight = if (isMet) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateNewPasswordScreenPreview() {
    GenecareTheme {
        CreateNewPasswordScreen(rememberNavController(), "kannasuneel04@gmail.com")
    }
}
