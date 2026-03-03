package com.simats.genecare.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.genecare.data.model.AppointmentDetailData
import com.simats.genecare.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionBillViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _appointmentDetails = MutableStateFlow<AppointmentDetailData?>(null)
    val appointmentDetails: StateFlow<AppointmentDetailData?> = _appointmentDetails.asStateFlow()

    private val _billDetails = MutableStateFlow<BillDetails?>(null)
    val billDetails: StateFlow<BillDetails?> = _billDetails.asStateFlow()

    private val _paymentState = MutableStateFlow<String>("Idle") // Idle, Processing, Success, Error
    val paymentState: StateFlow<String> = _paymentState.asStateFlow()

    data class BillDetails(
        val consultationFee: Double,
        val platformFee: Double,
        val gst: Double,
        val totalAmount: Double
    )

    fun loadBill(appointmentId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getAppointmentDetails(appointmentId)
                if (response.isSuccessful && response.body()?.status == "success") {
                    val appt = response.body()?.appointment
                    _appointmentDetails.value = appt
                    
                    if (appt != null) {
                        val fee = appt.consultationFee ?: 1000.0 // Default fallback
                        val platform = 50.0
                        val gst = (fee + platform) * 0.18
                        val total = fee + platform + gst
                        
                        _billDetails.value = BillDetails(
                            consultationFee = fee,
                            platformFee = platform,
                            gst = gst,
                            totalAmount = total
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    data class RazorpayCheckoutData(
        val orderId: String,
        val amount: Int,
        val keyId: String,
        val internalPaymentId: Int
    )

    private val _razorpayCheckoutData = MutableStateFlow<RazorpayCheckoutData?>(null)
    val razorpayCheckoutData: StateFlow<RazorpayCheckoutData?> = _razorpayCheckoutData.asStateFlow()

    fun processPayment(appointmentId: Int, method: String) {
        _paymentState.value = "Processing"
        viewModelScope.launch {
            try {
                val amount = _billDetails.value?.totalAmount ?: 0.0
                val response = repository.createPayment(appointmentId, amount, method)
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val body = response.body()!!
                    if (body.paymentId != null && body.razorpayOrderId != null && body.amount != null && body.keyId != null) {
                        _paymentState.value = "Idle" // Stop loading, wait for UI to launch Checkout
                        _razorpayCheckoutData.value = RazorpayCheckoutData(
                            orderId = body.razorpayOrderId,
                            amount = body.amount,
                            keyId = body.keyId,
                            internalPaymentId = body.paymentId
                        )
                    } else {
                        _paymentState.value = "Error: Invalid checkout details returned"
                    }
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "API Failed (${response.code()})"
                    _paymentState.value = "Error: $errorMsg"
                }
            } catch (e: Exception) {
                _paymentState.value = "Error: ${e.message}"
            }
        }
    }

    fun verifyPayment(paymentId: Int, orderId: String, razorpayPaymentId: String, signature: String, onSuccess: () -> Unit) {
        _paymentState.value = "Verifying"
        viewModelScope.launch {
            try {
                val response = repository.verifyPaymentSignature(paymentId, orderId, razorpayPaymentId, signature)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _paymentState.value = "Success"
                    onSuccess()
                } else {
                    _paymentState.value = "Error: Signature Verification Failed"
                }
            } catch (e: Exception) {
                _paymentState.value = "Error: ${e.message}"
            }
        }
    }

    fun setPaymentError(message: String) {
        _paymentState.value = "Error: $message"
    }

    private val _feedbackState = MutableStateFlow<String>("Idle") // Idle, Submitting, Success, Error
    val feedbackState: StateFlow<String> = _feedbackState.asStateFlow()

    fun submitFeedback(appointmentId: Int, rating: Int, comments: String) {
        val patientId = com.simats.genecare.data.UserSession.getUserId() ?: return
        _feedbackState.value = "Submitting"
        viewModelScope.launch {
            try {
                val response = repository.submitFeedback(appointmentId, patientId, rating, comments)
                if (response.isSuccessful && response.body()?.status == "success") {
                    _feedbackState.value = "Success"
                } else {
                    _feedbackState.value = "Error: ${response.body()?.message ?: "Submission failed"}"
                }
            } catch (e: Exception) {
                _feedbackState.value = "Error: ${e.message}"
            }
        }
    }

    fun clearCheckoutData() {
        _razorpayCheckoutData.value = null
    }
}
