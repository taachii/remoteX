package com.example.remotex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown

import com.example.remotex.R

@Composable
fun MediaControls(
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ArrowBack, stringResource(R.string.media_prev))
        }

        IconButton(onClick = onPlayPause) {
            Icon(Icons.Default.PlayArrow, stringResource(R.string.media_play))
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ArrowForward, stringResource(R.string.media_next))
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onVolumeDown) {
            Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.volume_down))
        }

        IconButton(onClick = onVolumeUp) {
            Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.volume_up))
        }
    }
}