package com.example.tasksync.ui.settings

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.tasksync.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val switchDarkMode =
            findViewById<Switch>(R.id.switchDarkMode)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            } else {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            }

        }
    }
}