package com.example.remotex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.remotex.network.RemoteXClient
import com.example.remotex.ui.screens.StartScreen
import com.example.remotex.ui.screens.TouchPadScreen
import com.example.remotex.ui.theme.RemoteXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RemoteXTheme {
                val context = LocalContext.current
                val client = remember { RemoteXClient(context) }
                var isConnected by remember { mutableStateOf(false) }
                var connectionError by remember { mutableStateOf<String?>(null) }

                if (connectionError != null) {
                    AlertDialog(
                        onDismissRequest = { connectionError = null },
                        title = { Text(stringResource(R.string.connection_error_title)) },
                        text = { Text(connectionError!!) },
                        confirmButton = {
                            Button(onClick = { connectionError = null }) {
                                Text(stringResource(R.string.ok))
                            }
                        }
                    )
                }

                if (!isConnected) {
                    StartScreen { ipAddress ->
                        client.connect(
                            ipAddress,
                            onSuccess = { isConnected = true },
                            onError = { error ->
                                connectionError = error
                            }
                        )
                    }
                } else {
                    TouchPadScreen(
                        client = client,
                        onDisconnect = {
                            client.disconnect()
                            isConnected = false
                        }
                    )
                }
            }
        }
    }
}