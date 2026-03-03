package com.simats.genecare.data.model

import com.google.gson.annotations.SerializedName

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val status: String,
    val message: String,
    val otp: String? = null  // Only returned in dev/demo mode
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class VerifyOtpResponse(
    val status: String,
    val message: String
)

data class ResetPasswordRequest(
    val email: String,
    @SerializedName("new_password") val newPassword: String
)

data class ResetPasswordResponse(
    val status: String,
    val message: String
)
