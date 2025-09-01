package com.example.dassscore

import android.app.Application
import com.google.firebase.FirebaseApp

class DassScoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
