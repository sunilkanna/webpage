package com.simats.genecare.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.genecare.ui.theme.GenecareTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    navController: NavController,
    viewModel: CreateAccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    // Password strength checks
    val hasMinLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val isPasswordStrong = hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
    var userType by remember { mutableStateOf("Patient") }

    val registrationState by viewModel.registrationState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(registrationState) {
        when (registrationState) {
            is RegistrationState.Success -> {
                val state = registrationState as RegistrationState.Success
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                
                state.user?.let { user ->
                    com.simats.genecare.data.UserSession.login(user)
                }
                
                if (userType == "Patient") {
                    navController.navigate("consent_and_privacy")
                } else {
                    navController.navigate("counselor_qualification")
                }
                viewModel.resetState()
            }
            is RegistrationState.Error -> {
                android.widget.Toast.makeText(context, (registrationState as RegistrationState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineLarge)
            Text("Join GeneCare to start your personalized health journey")
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() && it.isNotEmpty()
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError,
                supportingText = { if (emailError) Text("Invalid email address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordTouched = true },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                },
                isError = passwordTouched && !isPasswordStrong && password.isNotEmpty(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            // Password strength requirements
            if (passwordTouched && password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    Text("Password must have:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    PasswordRule("At least 8 characters", hasMinLength)
                    PasswordRule("An uppercase letter (A-Z)", hasUpperCase)
                    PasswordRule("A lowercase letter (a-z)", hasLowerCase)
                    PasswordRule("A digit (0-9)", hasDigit)
                    PasswordRule("A special character (!@#\$%^&*)", hasSpecialChar)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("I am a:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { userType = "Patient" },
                    modifier = Modifier.weight(1f),
                    colors = if (userType == "Patient") ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE0F2F1)) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Patient")
                }
                OutlinedButton(
                    onClick = { userType = "Counselor" },
                    modifier = Modifier.weight(1f),
                    colors = if (userType == "Counselor") ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE0F2F1)) else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Counselor")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            if (registrationState is RegistrationState.Loading) {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                            android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (!isEmailValid) {
                            emailError = true
                            android.widget.Toast.makeText(context, "Please enter a valid email", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (!isPasswordStrong) {
                            passwordTouched = true
                            android.widget.Toast.makeText(context, "Please create a stronger password", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            if (userType == "Patient") {
                                viewModel.register(fullName, email, password, "Patient")
                            } else {
                                viewModel.register(fullName, email, password, "Counselor")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080))
                ) {
                    Text("Continue")
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?")
                TextButton(onClick = { navController.navigate("sign_in") }) {
                    Text("Sign In")
                }
            }
        }
    }
}

@Composable
fun PasswordRule(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Text(
            text = if (isMet) "✓" else "✗",
            color = if (isMet) Color(0xFF4CAF50) else Color(0xFFE53935),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = "  $text",
            color = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CreateAccountScreenPreview() {
    GenecareTheme {
        CreateAccountScreen(NavController(androidx.compose.ui.platform.LocalContext.current))
    }
}
