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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.evertschavez.livestreamequis.R
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
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showOverlayControls by remember { mutableStateOf(false) }

    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls) {
            delay(3000)
            showOverlayControls = false
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopPlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                PlayerTopBar(title = stringResource(R.string.app_name), onBack = onBack)
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

            VideoPlayerSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isLandscape || !state.isUiVisible) Modifier.weight(1f) else Modifier.aspectRatio(
                            16f / 9f
                        )
                    ),
                player = viewModel.player,
                isPlaying = state.playerState == PlayerState.Playing,
                showOverlay = showOverlayControls,
                isUiVisible = state.isUiVisible,
                isLandscape = isLandscape,
                onToggleUi = viewModel::toggleUi,
                onToggleOverlay = { showOverlayControls = !showOverlayControls },
                onBack = onBack,
                onPlayPause = viewModel::onPlayPauseClicked,
                playerState = state.playerState
            )
            if (state.isUiVisible && !isLandscape) {
                MetricsSection(
                    modifier = Modifier.weight(1f),
                    state = state,
                    onToggleUi = viewModel::toggleUi,
                    onPlayPause = viewModel::onPlayPauseClicked
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerSection(
    modifier: Modifier,
    player: Player,
    isPlaying: Boolean,
    showOverlay: Boolean,
    isUiVisible: Boolean,
    isLandscape: Boolean,
    onToggleUi: () -> Unit,
    onToggleOverlay: () -> Unit,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    playerState: PlayerState,
) {
    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(onClick = onToggleOverlay),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply { this.player = player; useController = false }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isPlaying) {
            Text(
                text = stringResource(R.string.live_badge),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Red, RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Box(Modifier.fillMaxSize()) {
                if (!isUiVisible || isLandscape) {
                    IconButton(
                        onClick = onBack, Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.back_button_desc),
                            tint = Color.White
                        )
                    }
                }

                Box(Modifier.align(Alignment.Center)) {
                    PlaybackControlButton(playerState, onPlayPause, onPlayPause)
                }

                if (!isUiVisible) {
                    OutlinedButton(
                        onClick = onToggleUi,
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text(stringResource(R.string.show_ui), color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = Color.White) },
        colors = topAppBarColors(
            containerColor = Color.Black
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back_button_desc),
                    tint = Color.White,
                )
            }
        },
        actions = { CastButton(modifier = Modifier.padding(end = 16.dp)) },
    )
}

@Composable
private fun MetricsSection(
    modifier: Modifier,
    state: PlayerUiState,
    onToggleUi: () -> Unit,
    onPlayPause: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(state.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(state.subtitle, style = MaterialTheme.typography.titleSmall, color = Color.LightGray)

        Spacer(Modifier.height(16.dp))

        StatRow(stringResource(R.string.metric_codec), state.metrics.videoCodec)
        HorizontalDivider(color = Color.DarkGray)
        StatRow(stringResource(R.string.metric_bitrate), "${state.metrics.bitrateKbps} kbps")
        HorizontalDivider(color = Color.DarkGray)
        StatRow(stringResource(R.string.metric_container), state.metrics.videoFormat)
        HorizontalDivider(color = Color.DarkGray)
        StatRow(stringResource(R.string.metric_audio), state.metrics.audioCodec)
        HorizontalDivider(color = Color.DarkGray)

        Spacer(Modifier.weight(1f))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onToggleUi, border = BorderStroke(1.dp, Color.White)) {
                Text(stringResource(R.string.hide_ui), color = Color.White)
            }
            PlaybackControlButton(state.playerState, onPlayPause, onPlayPause)
        }
    }
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
