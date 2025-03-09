package com.example.remotex

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.remotex.ui.theme.RemoteXTheme
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.zip.Deflater

class MainActivity : ComponentActivity() {
    private var socket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private val serverPort = 5000
    private var isConnected by mutableStateOf(false)
    private val sensitivity = 2.5f  // Zwiększona czułość
    private val sendInterval = 20L  // Wysyłaj dane co 20 ms
    private var lastSendTime = 0L
    private var accumulatedDx = 0f
    private var accumulatedDy = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteXTheme {
                if (!isConnected) {
                    StartScreen { ipAddress ->
                        connectToServer(ipAddress)
                    }
                } else {
                    TouchPadScreen(onDisconnect = {
                        disconnectFromServer()
                    })
                }
            }
        }
    }

    private fun connectToServer(ipAddress: String) {
        Thread {
            try {
                serverAddress = InetAddress.getByName(ipAddress)
                socket = DatagramSocket()
                isConnected = true

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Połączono z serwerem", Toast.LENGTH_SHORT).show()
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Błąd połączenia z serwerem: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun disconnectFromServer() {
        Thread {
            try {
                socket?.close()
                isConnected = false

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Rozłączono z serwerem", Toast.LENGTH_SHORT).show()
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromServer()
    }

    private fun compressData(data: ByteArray): ByteArray {
        val deflater = Deflater()
        deflater.setInput(data)
        deflater.finish()
        val output = ByteArray(1024)
        val compressedSize = deflater.deflate(output)
        return output.copyOf(compressedSize)
    }

    fun sendDataToServer(deltaX: Float, deltaY: Float) {
        if (!isConnected) return

        accumulatedDx += deltaX * sensitivity
        accumulatedDy += deltaY * sensitivity

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSendTime >= sendInterval) {
            Thread {
                try {
                    val dx = accumulatedDx.toInt()
                    val dy = accumulatedDy.toInt()

                    if (dx != 0 || dy != 0) {
                        // Tworzenie JSON za pomocą org.json
                        val jsonData = JSONObject().apply {
                            put("dx", dx)
                            put("dy", dy)
                        }.toString()
                        val data = jsonData.toByteArray(Charsets.UTF_8)
                        val compressedData = compressData(data)

                        val packet = DatagramPacket(
                            compressedData,
                            compressedData.size,
                            serverAddress,
                            serverPort
                        )
                        socket?.send(packet)

                        accumulatedDx = 0f
                        accumulatedDy = 0f
                        lastSendTime = currentTime
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Błąd przesyłania danych: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    disconnectFromServer()
                }
            }.start()
        }
    }
}

@Composable
fun StartScreen(onConnectClicked: (String) -> Unit) {
    var ipAddress by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Adres IP komputera") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (ipAddress.isNotEmpty()) {
                    onConnectClicked(ipAddress)
                } else {
                    Toast.makeText(context, "Wprowadź adres IP", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Połącz")
        }
    }
}

@Composable
fun TouchPadScreen(onDisconnect: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val (dx, dy) = dragAmount
                    (context as MainActivity).sendDataToServer(dx, dy)
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = onDisconnect,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rozłącz")
            }
        }
    }
}