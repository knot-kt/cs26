package com.knotkt.cs26.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.knotkt.cs26.shared.Cs26App
import com.knotkt.cs26.shared.HealthRepository
import com.knotkt.cs26.shared.HealthState
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = HttpClient(CIO)
    private val healthRepository = HealthRepository(client, "http://10.0.2.2:8080")
    private var healthState by mutableStateOf(HealthState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Cs26App(
                healthState = healthState,
                onCheckHealth = ::checkHealth,
            )
        }
    }

    private fun checkHealth() {
        healthState = healthState.copy(isLoading = true, error = null)
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { healthRepository.check() }
            }
            healthState = result.fold(
                onSuccess = { response -> HealthState(response = response) },
                onFailure = { error -> HealthState(error = error.message ?: "request failed") },
            )
        }
    }

    override fun onDestroy() {
        client.close()
        scope.cancel()
        super.onDestroy()
    }
}
