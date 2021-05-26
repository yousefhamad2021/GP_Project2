package com.graduate.project.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import com.graduate.project.R
import com.graduate.project.util.isLoggedInKey
import com.graduate.project.util.loadSharedPreferences

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = loadSharedPreferences()

        setContentView(R.layout.activity_splash)
        val isLoggedIn = sharedPreferences.getBoolean(isLoggedInKey, false)

        Handler().postDelayed({
            navigateFromSplash(isLoggedIn)
        }, 2000)
    }

    private fun navigateFromSplash(isLoggedIn: Boolean) {
        val intentFromSplash: Intent = if (isLoggedIn) {
            Intent(this@SplashActivity, DashboardActivity::class.java)
        } else {
            Intent(this@SplashActivity, LoginActivity::class.java)
        }
        startActivity(intentFromSplash)
        ActivityCompat.finishAffinity(this@SplashActivity)
    }
}