package com.example.remotex

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import java.util.zip.Deflater

class MainActivity : ComponentActivity() {
    private var socket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private val serverPort = 5000
    private var isConnected by mutableStateOf(false)
    var isServerAlive by mutableStateOf(true)
    private val sensitivity = 0.7f
    private val sendInterval = 20L
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

    private fun isInSameNetwork(phoneIp: String, serverIp: String): Boolean {
        val phoneOctets = phoneIp.split(".").take(3)
        val serverOctets = serverIp.split(".").take(3)
        return phoneOctets == serverOctets
    }

    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun connectToServer(ipAddress: String) {
        Thread {
            try {
                val phoneIp = getLocalIpAddress()
                if (phoneIp == null || !isInSameNetwork(phoneIp, ipAddress)) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Adres IP nie znajduje się w tej samej sieci co telefon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@Thread
                }

                val serverAddr = InetAddress.getByName(ipAddress)
                val testSocket = DatagramSocket()
                testSocket.soTimeout = 2000

                val pingData = "ping".toByteArray()
                val pingPacket = DatagramPacket(pingData, pingData.size, serverAddr, serverPort)
                testSocket.send(pingPacket)

                val responseBuffer = ByteArray(1024)
                val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                testSocket.receive(responsePacket)

                val response = String(responsePacket.data, 0, responsePacket.length)
                if (response != "pong") {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Serwer nie odpowiada. Upewnij się, że serwer jest uruchomiony.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    testSocket.close()
                    return@Thread
                }

                testSocket.close()

                serverAddress = serverAddr
                socket = DatagramSocket()
                isConnected = true
                isServerAlive = true

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Połączono z serwerem", Toast.LENGTH_SHORT).show()
                }

                checkServerAlive()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Błąd połączenia z serwerem: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun checkServerAlive() {
        Thread {
            while (isConnected) {
                try {
                    val pingData = "ping".toByteArray()
                    val pingPacket = DatagramPacket(
                        pingData,
                        pingData.size,
                        serverAddress,
                        serverPort
                    )
                    socket?.send(pingPacket)

                    val responseBuffer = ByteArray(1024)
                    val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket?.soTimeout = 2000
                    socket?.receive(responsePacket)

                    val response = String(responsePacket.data, 0, responsePacket.length)
                    if (response == "pong") {
                        isServerAlive = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isServerAlive = false
                    disconnectFromServer()
                    break
                }
                Thread.sleep(5000)
            }
        }.start()
    }

    private fun disconnectFromServer() {
        Thread {
            try {
                socket?.close()
                isConnected = false
                isServerAlive = false

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
        if (!isConnected || socket == null || serverAddress == null) return

        accumulatedDx += deltaX * sensitivity
        accumulatedDy += deltaY * sensitivity

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSendTime >= sendInterval) {
            Thread {
                try {
                    val dx = accumulatedDx.toInt()
                    val dy = accumulatedDy.toInt()

                    if (dx != 0 || dy != 0) {
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

    fun sendTapToServer() {
        if (!isConnected || socket == null || serverAddress == null) return

        Thread {
            try {
                val jsonData = JSONObject().apply {
                    put("action", "tap")
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
    val activity = context as MainActivity

    LaunchedEffect(activity.isServerAlive) {
        if (!activity.isServerAlive) {
            onDisconnect()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val (dx, dy) = dragAmount
                    activity.sendDataToServer(dx, dy)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        activity.sendTapToServer()
                    }
                )
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