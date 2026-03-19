package com.simats.genecare

import android.app.Application
import com.simats.genecare.data.UserSession

class Genecare : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)
    }
}
