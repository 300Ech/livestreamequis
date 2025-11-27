package com.evertschavez.livestreamequis.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.google.android.gms.cast.framework.CastButtonFactory
import org.koin.androidx.compose.koinViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    url: String,
    adTag: String?,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val state by viewModel.playerState.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startPlayback(url, adTag)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LiveStreamEquis Player") },
                actions = {
                    CastButton(modifier = Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = viewModel.player
                        useController = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )

            Metrics(metrics = metrics)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.toUiText(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            PlaybackControlButton(
                state = state,
                onPlay = {
                    if (state == PlayerState.Paused) {
                        viewModel.resumePlayback()
                    } else {
                        viewModel.startPlayback(url, adTag)
                    }
                },
                onPause = { viewModel.pausePlayback() }
            )
        }
    }
}

@Composable
fun ColumnScope.Metrics(metrics: PlaybackMetrics) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Bitrate: ${metrics.bitrateKbps} kbps")
        Text("Rebuffers: ${metrics.rebufferCount}")
    }
}

fun PlayerState.toUiText(): String =
    when (this) {
        PlayerState.Idle -> "Idle"
        PlayerState.Buffering -> "Buffering…"
        PlayerState.Playing -> "Live"
        PlayerState.Paused -> "Paused"
        is PlayerState.Error -> "Error"
    }


@Composable
fun PlaybackControlButton(
    state: PlayerState,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    val (text, enabled, action) = when (state) {
        PlayerState.Idle -> Triple("Play Live", true, onPlay)
        PlayerState.Buffering -> Triple("Buffering…", false, {})
        PlayerState.Playing -> Triple("Pause", true, onPause)
        PlayerState.Paused -> Triple("Play", true, onPlay)
        is PlayerState.Error -> Triple("Retry", true, onPlay)
    }

    Button(
        onClick = action,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CastButton(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MediaRouteButton(context).apply {
                CastButtonFactory.setUpMediaRouteButton(context, this)
            }
        }
    )
}
