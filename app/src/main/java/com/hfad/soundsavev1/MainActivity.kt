package com.hfad.soundsavev1

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.hfad.soundsavev1.databinding.ActivityMainBinding
import java.io.IOException
import kotlin.math.log10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var volumeTextview: TextView
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    lateinit var binding: ActivityMainBinding
    private lateinit var redTrafficLightImageView: ImageView
    private lateinit var yellowTrafficLightImageView: ImageView
    private lateinit var greenTrafficLightImageView: ImageView
    private var isDangerous = false
    private lateinit var outputFilePath: String
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        volumeTextview = binding.volumeTextview
        val recommendationsTextView = binding.recommendations

        val startButton = binding.startButton
        startButton.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
        startButton.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions()) {
                    startRecording()
                } else {
                    requestPermissions()
                }
            } else {
                stopRecording()
            }
            fetchAdvice()

        }

        redTrafficLightImageView = binding.redTrafficLightImageView
        yellowTrafficLightImageView = binding.yellowTrafficLightImageView
        greenTrafficLightImageView = binding.greenTrafficLightImageView

        outputFilePath = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(outputFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        }
            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
                isRecording = true
                binding.startButton.text = "Stop"
                binding.startButton.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                displayStatus("Recording...")
                handler.post(updateDecibelLevelTask)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MediaRecorder", "prepare() failed")
                Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                mediaRecorder.release()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Log.e("MediaRecorder", "start() failed")
                Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                mediaRecorder.release()
            }
        }

        private fun stopRecording() {
            try {
                handler.removeCallbacks(updateDecibelLevelTask)
                mediaRecorder.stop()
                mediaRecorder.release()
                isRecording = false
                binding.startButton.text = "Start"
                binding.startButton.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                displayStatus("Stopped")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Stop recording failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        private fun checkPermissions(): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun requestPermissions() {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        private fun displayStatus(message: String) {
            volumeTextview.text = message
            volumeTextview.visibility = View.VISIBLE
        }

        fun updateTrafficLight(decibelLevel: Int) {
            Log.d("UpdateTrafficLight", "Decibel Level: $decibelLevel")
            when {
                decibelLevel <= 60 -> {
                    redTrafficLightImageView.setImageResource(R.drawable.red_dark_circle)
                    yellowTrafficLightImageView.setImageResource(R.drawable.yellow_dark_circle)
                    greenTrafficLightImageView.setImageResource(R.drawable.green_light_circle)
                    binding.recommendations.text = getString(R.string.green_light)
                    isDangerous = false
                }

                decibelLevel <= 90 -> {
                    redTrafficLightImageView.setImageResource(R.drawable.red_dark_circle)
                    yellowTrafficLightImageView.setImageResource(R.drawable.yellow_light_circle)
                    greenTrafficLightImageView.setImageResource(R.drawable.green_dark_circle)
                    binding.recommendations.text = getString(R.string.yellow_light)
                    isDangerous = false
                }

                else -> {
                    redTrafficLightImageView.setImageResource(R.drawable.red_light_circle)
                    yellowTrafficLightImageView.setImageResource(R.drawable.yellow_dark_circle)
                    greenTrafficLightImageView.setImageResource(R.drawable.green_dark_circle)
                    binding.recommendations.text = getString(R.string.red_light)
                    isDangerous = true
                }
            }
            // Display the decibel level
            volumeTextview.text =  getString(R.string.decibel_level, decibelLevel)
        }

    private val updateDecibelLevelTask = object : Runnable {
        override fun run() {
            if (isRecording) {
                val decibelLevel = measureDecibelLevel()
                Log.d("DecibelLevel", "Measured decibel level: $decibelLevel")
                updateTrafficLight(decibelLevel)
                handler.postDelayed(this, 1000) // Update every second
            }
        }
    }

    fun measureDecibelLevel(): Int {
        return if (::mediaRecorder.isInitialized) {
            val maxAmplitude = mediaRecorder.maxAmplitude
            if (maxAmplitude > 0) {
                (20 * log10(maxAmplitude.toDouble())).toInt()
            } else {
                0
            }
        } else {
            0
        }
    }

        override fun onPause() {
            super.onPause()
            if (isDangerous) {
                Toast.makeText(this, "Dangerous noise level detected!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startRecording()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAdvice() {
        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<AdviceSlipResponse> = RetrofitClient.adviceSlipService.getAdvice().execute()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val advice = response.body()?.slip?.advice
                    binding.advice.text = advice ?: "No advice found"
                } else {
                    Log.e("MainActivity", "Error fetching advice: ${response.code()}")
                    binding.advice.text = "Error fetching advice"
                }
            }
        }
    }
}
