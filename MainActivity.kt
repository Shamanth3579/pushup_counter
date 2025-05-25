package com.example.pushupcounter

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvCount: TextView
    private lateinit var btnReset: Button
    private lateinit var progressBar: ProgressBar
    private var tts: TextToSpeech? = null

    private var autoFetchJob: Job? = null
    private var lastCount: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prevent screen from turning off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tvCount = findViewById(R.id.tvCount)
        btnReset = findViewById(R.id.btnReset)
        progressBar = findViewById(R.id.progressBar)
        tts = TextToSpeech(this, this)

        btnReset.setOnClickListener {
            resetPushupCount()
        }
    }

    override fun onResume() {
        super.onResume()
        // Start auto-fetching pushup count every second
        autoFetchJob = lifecycleScope.launch {
            while (isActive) {
                fetchPushupCount()
                delay(1000) // 1 second polling interval
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop auto-fetch when activity is not visible
        autoFetchJob?.cancel()
    }

    private fun fetchPushupCount() {
        showLoading(false) // Don't show loading spinner for polling
        lifecycleScope.launch {
            val count = getPushupCountFromESP()
            if (count >= 0) {
                tvCount.text = getString(R.string.pushup_count, count)
                if (lastCount != -1 && count > lastCount) {
                    // Only announce if count increased
                    tts?.speak(count.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
                }
                lastCount = count
            }
        }
    }

    private suspend fun getPushupCountFromESP(): Int = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://192.168.4.1/")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            conn.requestMethod = "GET"
            val response = conn.inputStream.bufferedReader().readText().trim()
            conn.disconnect()
            response.toIntOrNull() ?: -1
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            -1
        }
    }

    private fun resetPushupCount() {
        showLoading(true)
        lifecycleScope.launch {
            val success = resetCountOnESP()
            showLoading(false)
            if (success) {
                tvCount.text = getString(R.string.pushup_count, 0)
                lastCount = 0
            } else {
                Toast.makeText(this@MainActivity, "Failed to reset count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun resetCountOnESP(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://192.168.4.1/reset") // ESP32 must handle this endpoint
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            conn.requestMethod = "GET"
            conn.inputStream.close()
            conn.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnReset.isEnabled = !show
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            // Try to pick a female US English voice if available, else default
            val voices = tts?.voices
            voices?.firstOrNull {
                it.locale == Locale.US && it.name.contains("female", ignoreCase = true)
            }?.let { voice ->
                tts?.voice = voice
            }
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
