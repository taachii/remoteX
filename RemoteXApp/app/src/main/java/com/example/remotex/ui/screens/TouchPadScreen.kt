package com.example.remotex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.remotex.R
import com.example.remotex.network.RemoteXClient
import com.example.remotex.ui.components.MediaControls

@Composable
fun TouchPadScreen(
    client: RemoteXClient,
    onDisconnect: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Główny obszar touchpada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF838383))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        client.sendMovement(dragAmount.x, dragAmount.y)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { client.sendClick() },
                        onLongPress = { client.sendRightClick() }
                    )
                }
        )

        // Kontrolki multimedialne
        MediaControls(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            onPlayPause = { client.sendMediaCommand("play_pause") },
            onNext = { client.sendMediaCommand("next") },
            onPrevious = { client.sendMediaCommand("previous") },
            onVolumeUp = { client.sendMediaCommand("volume_up") },
            onVolumeDown = { client.sendMediaCommand("volume_down") }
        )

        // Przycisk rozłączania
        Button(
            onClick = onDisconnect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(stringResource(R.string.disconnect_button))
        }
    }
}