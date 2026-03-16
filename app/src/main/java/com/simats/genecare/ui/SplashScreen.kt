
package com.simats.genecare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.simats.genecare.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.genecare.ui.theme.GenecareTheme
import com.simats.genecare.data.UserSession
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(3000L) // 3-second delay
        
        val nextDestination = if (UserSession.isLoggedIn()) {
            when (UserSession.getUserType()) {
                "Patient" -> "dashboard"
                "Doctor/Counselor", "Counselor" -> {
                    // Route to dashboard if approved, else pending
                    val user = UserSession.getUser()
                    if (user?.verificationStatus == "Approved") "counselor_dashboard"
                    else "counselor_pending_dashboard"
                }
                "Admin" -> "admin_dashboard"
                else -> "dashboard"
            }
        } else {
            "welcome"
        }

        navController.navigate(nextDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF008080),
                        Color(0xFF00C4C4)
                    )
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // You will need to add your logo to the drawable folder
        // and reference it here. For now, I'm using a placeholder.
        // Icon(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "GeneCare Logo")
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "GeneCare Logo",
            modifier = Modifier
                .size(150.dp) // Adjust size as needed
                .padding(bottom = 16.dp)
        )
        Text(
            text = "Curogenea",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Personalized Genetic Counseling & Follow-Up Care",
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    GenecareTheme {
        // This is a dummy NavController for preview purposes
        val navController = NavController(androidx.compose.ui.platform.LocalContext.current)
        SplashScreen(navController)
    }
}
