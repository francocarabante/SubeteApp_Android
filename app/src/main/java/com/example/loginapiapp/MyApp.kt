package com.example.loginapiapp

import android.app.Application
import com.google.android.libraries.places.api.Places

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBnDDeYhxpb6H8zyDJA38h7k_Xs-HT5OB4")
        }
    }
}