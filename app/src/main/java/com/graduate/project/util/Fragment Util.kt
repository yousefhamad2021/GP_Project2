package com.graduate.project.util

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.graduate.project.R

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun Fragment.loadSharedPreferences(): SharedPreferences {
    return requireActivity().getSharedPreferences(
        getString(R.string.preferences_file_name),
        Context.MODE_PRIVATE
    )
}