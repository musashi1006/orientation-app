package com.example.orientationapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BatteryActivity : AppCompatActivity() {

    private lateinit var batteryTextView: TextView

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                val temperature = temp / 10.0
                batteryTextView.text = "バッテリー温度: $temperature ℃"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        batteryTextView = TextView(this)
        batteryTextView.textSize = 24f
        setContentView(batteryTextView)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }
}

