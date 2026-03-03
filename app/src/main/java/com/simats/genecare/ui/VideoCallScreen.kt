package com.simats.genecare.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.PermissionRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoCallScreen(navController: NavController, appointmentId: Int = 1) {
    val viewModel: VideoCallViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var isMicOn by remember { mutableStateOf(true) }
    var isCameraOn by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    var hasPermissions by remember { mutableStateOf(false) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        hasPermissions = cameraGranted && audioGranted
        if (hasPermissions) {
            viewModel.startSession(appointmentId)
        }
    }

    LaunchedEffect(Unit) {
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        
        if (hasCamera && hasAudio) {
            hasPermissions = true
            viewModel.startSession(appointmentId)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        when {
            !hasPermissions -> {
                // Permissions not granted state
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Permissions Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera and Microphone permissions are needed to join the video call.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
            
            state.isLoading -> {
                // Loading state
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF00ACC1))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Joining session...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            state.meetingLink != null -> {
                // Jitsi WebView - Full screen video call
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.allowContentAccess = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            settings.javaScriptCanOpenWindowsAutomatically = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            settings.setSupportMultipleWindows(true)
                            
                            android.util.Log.d("VideoCall", "Loading URL: ${state.meetingLink}")
                            
                            // Desktop User Agent to ensure Jitsi renders full interface
                            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    if (url == null) return false
                                    // Block Jitsi from trying to open external app schemes (intent://, org.jitsi.meet://, etc.)
                                    // CRITICAL: Must allow wss:// for WebRTC signaling
                                    return if (url.startsWith("http://") || url.startsWith("https://") || 
                                               url.startsWith("ws://") || url.startsWith("wss://") ||
                                               url.startsWith("blob:")) {
                                        false // Allow web navigation
                                    } else {
                                        true // Block special schemes
                                    }
                                }
                            }

                            // Handle camera/mic permissions
                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    val requestedResources = request?.resources ?: emptyArray()
                                    // Only grant if the Android OS has also granted us permissions
                                    val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    
                                    val filteredResources = mutableListOf<String>()
                                    if (requestedResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) && hasCamera) {
                                        filteredResources.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                                    }
                                    if (requestedResources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) && hasMic) {
                                        filteredResources.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                                    }
                                    
                                    if (filteredResources.isNotEmpty()) {
                                        request?.grant(filteredResources.toTypedArray())
                                    } else {
                                        request?.deny()
                                    }
                                }
                            }

                            webViewRef = this
                            loadUrl(state.meetingLink!!)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp) // Space for controls
                )

                // Bottom Controls overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mic Button
                    IconButton(
                        onClick = {
                            isMicOn = !isMicOn
                            webViewRef?.evaluateJavascript(
                                "document.querySelector('[aria-label*=\"mic\"], [aria-label*=\"Mic\"]')?.click()",
                                null
                            )
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isMicOn) Color(0xFF263238) else Color(0xFFD32F2F),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isMicOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                            contentDescription = "Mic",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // End Call Button
                    IconButton(
                        onClick = {
                            webViewRef?.destroy()
                            viewModel.endCall(appointmentId) {
                                val userType = com.simats.genecare.data.UserSession.getUserType()
                                if (userType == "Patient") {
                                    navController.navigate("session_bill/$appointmentId") {
                                        popUpTo("video_call/$appointmentId") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("counselor_dashboard") {
                                        popUpTo("video_call/$appointmentId") { inclusive = true }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFD32F2F), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Camera Button
                    IconButton(
                        onClick = {
                            isCameraOn = !isCameraOn
                            webViewRef?.evaluateJavascript(
                                "document.querySelector('[aria-label*=\"camera\"], [aria-label*=\"Camera\"]')?.click()",
                                null
                            )
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isCameraOn) Color(0xFF263238) else Color(0xFFD32F2F),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isCameraOn) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }

                    // View Patient Report (Counselor Only)
                    val userType = com.simats.genecare.data.UserSession.getUserType()
                    if (userType != "Patient" && !state.medicalReportUrl.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(20.dp))
                        IconButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(state.medicalReportUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF00ACC1), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = "View Report",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            else -> {
                // Error / Not ready state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connection Details:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = state.meetingLink ?: "No URL generated",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row {
                        Button(
                            onClick = { viewModel.startSession(appointmentId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
