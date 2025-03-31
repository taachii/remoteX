package com.example.remotex.network

import android.content.Context
import android.util.Log
import com.example.remotex.R
import org.json.JSONObject
import java.net.*

class RemoteXClient(private val context: Context) {
    private var socket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private val serverPort = 5000
    private var isConnected = false
    private val sensitivity = 0.7f
    private val sendInterval = 20L
    private var lastSendTime = 0L
    private var accumulatedDx = 0f
    private var accumulatedDy = 0f

    fun connect(ipAddress: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val serverAddr = InetAddress.getByName(ipAddress)
                serverAddress = serverAddr
                socket = DatagramSocket()
                isConnected = true
                onSuccess()
            } catch (e: Exception) {
                onError(context.getString(R.string.connection_error, e.message ?: "Unknown error"))
            }
        }.start()
    }

    fun sendMovement(deltaX: Float, deltaY: Float) {
        if (!isConnected) return

        accumulatedDx += deltaX * sensitivity
        accumulatedDy += deltaY * sensitivity

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSendTime >= sendInterval) {
            val dx = accumulatedDx.toInt()
            val dy = accumulatedDy.toInt()

            if (dx != 0 || dy != 0) {
                sendJsonData(JSONObject().apply {
                    put("dx", dx)
                    put("dy", dy)
                })
                accumulatedDx = 0f
                accumulatedDy = 0f
                lastSendTime = currentTime
            }
        }
    }

    fun sendClick() = sendAction("tap")
    fun sendRightClick() = sendAction("right_click")

    fun sendMediaCommand(command: String) {
        sendJsonData(JSONObject().apply {
            put("action", "media")
            put("command", command)
        })
    }

    private fun sendAction(action: String) {
        sendJsonData(JSONObject().apply {
            put("action", action)
        })
    }

    private fun sendJsonData(jsonData: JSONObject) {
        if (!isConnected) return

        Thread {
            try {
                val data = jsonData.toString().toByteArray()
                socket?.send(DatagramPacket(
                    data,
                    data.size,
                    serverAddress,
                    serverPort
                ))
            } catch (e: Exception) {
                Log.e("RemoteX", "Send error", e)
                disconnect()
            }
        }.start()
    }

    fun disconnect() {
        socket?.close()
        isConnected = false
    }
}