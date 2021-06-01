package com.graduate.project.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.graduate.project.R
import com.graduate.project.activity.RestaurantDetailActivity
import kotlinx.android.synthetic.main.scan_page.*

import java.util.*


class ScanFragment :   AppCompatActivity()  {
    private lateinit var codeScanner: CodeScanner

    private val REQUEST_CODE_PERMISSIONS = 1001

    private val REQUIRED_PERMISSIONS = arrayOf(
        "android.permission.CAMERA",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WAKE_LOCK"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContentView(R.layout.scan_page)
        if (allPermissionsGranted()) {
            startQrCode()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }




    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this as Context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun startQrCode() {

        codeScanner = scanner_view?.let { CodeScanner(this, it) }!!
        codeScanner.decodeCallback = DecodeCallback {
            this.runOnUiThread {
                val textQr = it.text
                if (textQr == "Hello :)") {
                    val intent = Intent(this, RestaurantDetailActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(this,"Not Found",Toast.LENGTH_SHORT).show()
                }

            }
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                codeScanner.releaseResources()
            }
        }, 1000)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                codeScanner.startPreview()
            }
        }, 2000)


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startQrCode()

            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


}


