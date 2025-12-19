package com.evertschavez.livestreamequis.ui.player

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.google.android.gms.cast.framework.CastButtonFactory
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    url: String,
    adTag: String?,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showOverlayControls by remember { mutableStateOf(false) }

    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls) {
            delay(3000)
            showOverlayControls = false
        }
    }

    DisposableEffect(Unit) {
        viewModel.initialize(url, adTag, title, subtitle)
        onDispose { viewModel.stopPlayback() }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            if (state.isUiVisible && !isLandscape) {
                TopAppBar(
                    title = { Text("LiveStreamEquis", color = Color.White) },
                    colors = topAppBarColors(
                        containerColor = Color.Black
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    },
                    actions = { CastButton(modifier = Modifier.padding(end = 16.dp)) }
                )
            }
        }
    ) { paddingValues ->
        val realPadding =
            if (state.isUiVisible && !isLandscape) paddingValues else PaddingValues(0.dp)

        Column(
            modifier = Modifier
                .padding(realPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isLandscape || !state.isUiVisible) Modifier.weight(1f) else Modifier.aspectRatio(
                            16f / 9f
                        )
                    )
                    .background(Color.Black)
                    .clickable {
                        showOverlayControls = !showOverlayControls
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = viewModel.player
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (state == PlayerState.Playing) {
                    Text(
                        text = "🔴 LIVE",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlayControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            PlaybackControlButton(
                                state = state.playerState,
                                onPlay = viewModel::onPlayPauseClicked,
                                onPause = viewModel::onPlayPauseClicked
                            )
                        }
                    }
                }
            }

            if (state.isUiVisible && !isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Metrics(state.metrics)

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(color = Color.DarkGray)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleUi() },
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text("Hide UI", color = Color.White)
                        }
                        PlaybackControlButton(
                            state = state.playerState,
                            onPlay = viewModel::onPlayPauseClicked,
                            onPause = viewModel::onPlayPauseClicked,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Metrics(metrics: PlaybackMetrics) {
    StatRow("Time to encode", "04:14")
    HorizontalDivider(color = Color.DarkGray)

    StatRow("Codec", metrics.videoCodec)
    HorizontalDivider(color = Color.DarkGray)

    StatRow("Container", metrics.videoFormat)
    HorizontalDivider(color = Color.DarkGray)

    StatRow("Audio", metrics.audioCodec)
    HorizontalDivider(color = Color.DarkGray)

    StatRow("Bitrate", "${metrics.bitrateKbps} kbps")
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun PlaybackControlButton(
    state: PlayerState,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var icon: ImageVector? = null
        var action: (() -> Unit)? = null
        when (state) {
            PlayerState.Playing -> {
                icon = Icons.Filled.Pause
                action = onPause
            }

            PlayerState.Idle, PlayerState.Paused, PlayerState.Ended -> {
                icon = Icons.Filled.PlayArrow
                action = onPlay
            }

            is PlayerState.Error -> {
                icon = Icons.Filled.Refresh
                action = onPlay
            }

            else -> {}
        }
        if (state == PlayerState.Buffering) {
            CircularProgressIndicator(
                color = Color.White
            )
        } else if (icon != null && action != null) {
            IconButton(
                onClick = action,
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
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
