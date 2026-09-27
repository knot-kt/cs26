package com.knotkt.cs26.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Cs26App(
    healthState: HealthState,
    onCheckHealth: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("CS26", style = MaterialTheme.typography.headlineMedium)
                Text("Health vertical slice", modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = when {
                        healthState.isLoading -> "Checking server..."
                        healthState.response != null -> "Server: ${healthState.response.status}"
                        healthState.error != null -> "Server unavailable: ${healthState.error}"
                        else -> "Server status not checked"
                    },
                    modifier = Modifier.padding(top = 16.dp),
                )
                Button(
                    onClick = onCheckHealth,
                    enabled = !healthState.isLoading,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(if (healthState.isLoading) "Checking" else "Check server")
                }
            }
        }
    }
}
