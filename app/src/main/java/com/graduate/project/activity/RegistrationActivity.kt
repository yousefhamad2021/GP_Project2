package com.graduate.project.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.VolleyError
import com.google.android.material.textfield.TextInputEditText
import com.graduate.project.*
import com.graduate.project.network.ConnectionManager
import com.graduate.project.network.NetworkTask
import com.graduate.project.network.noInternetDialog
import com.graduate.project.helper.*
import kotlinx.android.synthetic.main.content_profile.*
import org.json.JSONObject

class RegistrationActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var progressLayout: ProgressBar
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etMobileNumber: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button

    private lateinit var networkTaskListener: NetworkTask.NetworkTaskListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = loadSharedPreferences()

        setContentView(R.layout.activity_registration)
        title = "Register Yourself"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.hide()
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val userId = "1"
            val userName = etName.text.toString()
            val userEmail = etEmail.text.toString()
            val userMobileNumber = etMobileNumber.text.toString()
            val userAddress = ""
            saveToPreferences(
                userId, userName, userEmail, userMobileNumber, userAddress
            )
            // Since, we are navigating from RegistrationActivity and stopping the activity(finish())
            // after navigation, we do not need to enable the disabled Register button.
            navigateToDashboardActivity()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navigateToLoginActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToLoginActivity()
    }

    private fun tryRegister() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val mobile = etMobileNumber.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        when (checkValidInputs(name, email, mobile, password, confirmPassword)) {
            InputState.INVALID_NAME -> {
                etName.error = "Name should contain minimum 3 characters."
            }
            InputState.INVALID_EMAIL -> {
                etEmail.error = "Invalid Email."
            }
            InputState.INVALID_MOBILE -> {
                etMobileNumber.error = "Invalid mobile number."
            }

            InputState.INVALID_PASSWORD -> {
                etPassword.error = "Password should contain minimum 6 characters."
            }
            InputState.PSW_NO_MATCH -> {
                etConfirmPassword.error = "Passwords do not match!"
            }

            else -> {
                showToast("Unknown Input state.")
            }
        }

    }

    private fun checkValidInputs(
        name: String, email: String, mobile: String, password: String, confirmPassword: String
    ): InputState {
        return when {
            name.trim().length < 3 -> {
                InputState.INVALID_NAME
            }
            !email.matches(emailRegex) -> {
                InputState.INVALID_EMAIL
            }
            mobile.trim().length != 10 -> {
                InputState.INVALID_MOBILE
            }
            password.length < 6 -> {
                InputState.INVALID_PASSWORD
            }
            password.compareTo(confirmPassword) != 0 -> {
                InputState.PSW_NO_MATCH
            }
            else -> {
                InputState.OKAY
            }
        }
    }

    private fun sendNetworkRequest(
        name: String, mobileNumber: String, password: String, address: String, email: String
    ) {
        if (ConnectionManager().checkConnectivity(this@RegistrationActivity)) {
            progressLayout.show()
            btnRegister.disable()

            setupNetworkTaskListener()

            val jsonParams = JSONObject()
            jsonParams.put("name", name)
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", password)
            jsonParams.put("address", address)
            jsonParams.put("email", email)

            NetworkTask(networkTaskListener).makeNetworkRequest(
                this@RegistrationActivity, Request.Method.POST, "", jsonParams
            )
        } else {
            noInternetDialog(this@RegistrationActivity)
        }
    }

    private fun setupNetworkTaskListener() {
        networkTaskListener =
            object : NetworkTask.NetworkTaskListener {
                override fun onSuccess(result: JSONObject) {
                    progressLayout.hide()

                    try {
                        val returnObject = result.getJSONObject("data")
                        val success = returnObject.getBoolean("success")

                        if (success) {
                            val data = returnObject.getJSONObject("data")
                            val userId = data.getString("user_id")
                            val userName = data.getString("name")
                            val userEmail = data.getString("email")
                            val userMobileNumber = data.getString("mobile_number")
                            val userAddress = data.getString("address")
                            saveToPreferences(
                                userId, userName, userEmail, userMobileNumber, userAddress
                            )
                            // Since, we are navigating from RegistrationActivity and stopping the activity(finish())
                            // after navigation, we do not need to enable the disabled Register button.
                            navigateToDashboardActivity()
                        } else {
                            btnRegister.enable()
                            val errorMessage = returnObject.getString("errorMessage")
                            showToast(errorMessage)
                        }
                    } catch (e: Exception) {
                        btnRegister.enable()
                        showToast("Error: ${e.localizedMessage}")
                    }
                }

                override fun onFailed(error: VolleyError) {
                    btnRegister.enable()
                    showToast("Error: ${error.localizedMessage}")
                }
            }
    }

    private fun saveToPreferences(
        userId: String, name: String, email: String, mobile: String, address: String
    ) {
        sharedPreferences.edit()
            .putBoolean(isLoggedInKey, true)
            .putString(userIdKey, userId)
            .putString(userNameKey, name)
            .putString(userMobileKey, mobile)
            .putString(userEmailKey, email)
            .putString(userAddressKey, address)
            .apply()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
        startActivity(intent)
        ActivityCompat.finishAffinity(this@RegistrationActivity)
    }

    private fun navigateToDashboardActivity() {
        val intent = Intent(this@RegistrationActivity, DashboardActivity::class.java)
        startActivity(intent)
        ActivityCompat.finishAffinity(this@RegistrationActivity)
    }
}