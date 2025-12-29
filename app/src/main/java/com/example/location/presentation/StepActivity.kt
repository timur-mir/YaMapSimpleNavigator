package com.example.location.presentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.location.R
import com.example.location.databinding.ActivityMainBinding
import com.example.location.databinding.ActivityStepBinding

class StepActivity : AppCompatActivity(),SensorEventListener {
    private var _binding: ActivityStepBinding?=null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityStepBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.goPrev.setOnClickListener{
            jumpToNextActivity()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "Сенсор движения не найден!", Toast.LENGTH_LONG).show()
        }
        loadData()
        resetSteps()
    }
    override fun onStart() {
        super.onStart()
        registerSensor()
    }
    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }
    override fun onResume() {
        super.onResume()
        running = true
        registerSensor()
    }
    override fun onPause() {
        super.onPause()
        running = false
    }
    private fun registerSensor() {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Toast.makeText(this, "Успешно!", Toast.LENGTH_LONG).show()
        }
    }
    private fun resetSteps() {
       binding.tvStepsTaken.setOnClickListener {
            Toast.makeText(this, "Долгое нажатие для старта", Toast.LENGTH_SHORT).show()
        }

       binding.tvStepsTaken.setOnLongClickListener {
           previousTotalSteps = totalSteps
           binding.tvStepsTaken.text = "0"
           saveData()
            true
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (running) {
            totalSteps = event.values[0]
            val currentSteps = (totalSteps - previousTotalSteps).toInt()
          binding.tvStepsTaken.text = "$currentSteps"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        previousTotalSteps = sharedPreferences.getFloat("key1", 0f)
    }
    private fun jumpToNextActivity() {
        val intent = Intent(this@StepActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}