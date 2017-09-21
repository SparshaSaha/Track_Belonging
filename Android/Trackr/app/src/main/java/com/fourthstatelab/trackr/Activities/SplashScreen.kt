package com.fourthstatelab.trackr.Activities

import android.content.Intent
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.fourthstatelab.trackr.R

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({ startActivity(Intent(this@SplashScreen, Login::class.java)) }, 500)

    }
}
