package com.simats.genecare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.simats.genecare.ui.navgraph.NavGraph
import com.simats.genecare.ui.theme.GenecareTheme

import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

object RazorpayCallbackObject {
    var onSuccess: ((PaymentData?) -> Unit)? = null
    var onError: ((Int, String?) -> Unit)? = null
}

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.razorpay.Checkout.preload(applicationContext)
        com.simats.genecare.data.UserSession.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            GenecareTheme {
                NavGraph(startDestination = "splash")
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        RazorpayCallbackObject.onSuccess?.invoke(paymentData)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        RazorpayCallbackObject.onError?.invoke(code, response)
    }
}
