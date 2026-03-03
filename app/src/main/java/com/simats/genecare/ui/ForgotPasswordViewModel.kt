package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.model.ResetPasswordRequest
import com.simats.genecare.data.model.SendOtpRequest
import com.simats.genecare.data.model.VerifyOtpRequest
import com.simats.genecare.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val api = ApiClient.api

    private val _sendOtpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val sendOtpState: StateFlow<OtpState> = _sendOtpState

    private val _verifyOtpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val verifyOtpState: StateFlow<OtpState> = _verifyOtpState

    private val _resetPasswordState = MutableStateFlow<OtpState>(OtpState.Idle)
    val resetPasswordState: StateFlow<OtpState> = _resetPasswordState

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _sendOtpState.value = OtpState.Loading
            try {
                val response = api.sendOtp(SendOtpRequest(email))
                if (response.isSuccessful && response.body()?.status == "success") {
                    _sendOtpState.value = OtpState.Success(response.body()?.message ?: "OTP sent")
                } else {
                    _sendOtpState.value = OtpState.Error(response.body()?.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _sendOtpState.value = OtpState.Error(e.message ?: "Network error")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _verifyOtpState.value = OtpState.Loading
            try {
                val response = api.verifyOtp(VerifyOtpRequest(email, otp))
                if (response.isSuccessful && response.body()?.status == "success") {
                    _verifyOtpState.value = OtpState.Success(response.body()?.message ?: "Verified")
                } else {
                    _verifyOtpState.value = OtpState.Error(response.body()?.message ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _verifyOtpState.value = OtpState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = OtpState.Loading
            try {
                val response = api.resetPassword(ResetPasswordRequest(email, newPassword))
                if (response.isSuccessful && response.body()?.status == "success") {
                    _resetPasswordState.value = OtpState.Success(response.body()?.message ?: "Password reset")
                } else {
                    _resetPasswordState.value = OtpState.Error(response.body()?.message ?: "Reset failed")
                }
            } catch (e: Exception) {
                _resetPasswordState.value = OtpState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetSendOtpState() { _sendOtpState.value = OtpState.Idle }
    fun resetVerifyOtpState() { _verifyOtpState.value = OtpState.Idle }
    fun resetResetPasswordState() { _resetPasswordState.value = OtpState.Idle }
}

sealed class OtpState {
    object Idle : OtpState()
    object Loading : OtpState()
    data class Success(val message: String) : OtpState()
    data class Error(val message: String) : OtpState()
}
