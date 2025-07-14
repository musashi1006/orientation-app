package com.example.orientationapp

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var textView: TextView

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var ambientTemperatureSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // レイアウトを縦並びにする
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        // センサー値表示用TextView
        textView = TextView(this)
        layout.addView(textView)

        //temperatureTextView = TextView(this)
        //setContentView(temperatureTextView)

        // BatteryActivity起動ボタン
        val button = Button(this)
        button.text = "バッテリー温度を表示"
        button.setOnClickListener {
            val intent = Intent(this, BatteryActivity::class.java)
            startActivity(intent)
        }
        layout.addView(button)

        setContentView(layout)

        // センサー初期化
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // ambient temperature
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (ambientTemperatureSensor == null) {
            textView.append("\n周囲温度センサーが搭載されていません")
        }

        // バッテリー温度取得
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)
        val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val temperature = temp / 10.0
        textView.append("\nバッテリー温度: $temperature ℃")
    }

    // SensorEventListenerの実装
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
        }
        val R = FloatArray(9)
        val I = FloatArray(9)
        val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuth = Math.toDegrees(orientation[0].toDouble())
            val pitch = Math.toDegrees(orientation[1].toDouble())
            val roll = Math.toDegrees(orientation[2].toDouble())
            textView.text = "Azimuth: %.1f°\nPitch: %.1f°\nRoll: %.1f°".format(azimuth, pitch, roll)
            // バッテリー温度も再表示
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = registerReceiver(null, intentFilter)
            val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val temperature = temp / 10.0
            textView.append("\nバッテリー温度: $temperature ℃")
        }
        if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            val temperature = event.values[0]
            textView.append = "周囲温度: $temperature ℃"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 必要ならここに処理を書く
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        ambientTemperatureSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}

