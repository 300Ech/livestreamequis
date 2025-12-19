package com.evertschavez.livestreamequis.player.core.controller

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import com.evertschavez.livestreamequis.player.core.exoplayer.ExoPlayerFactory
import com.evertschavez.livestreamequis.player.core.metrics.PlayerMetricsTracker
import com.evertschavez.livestreamequis.player.domain.controller.PlayerController
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.evertschavez.livestreamequis.player.domain.model.StreamConfig
import com.mux.stats.sdk.muxstats.MuxStatsSdkMedia3
import com.mux.stats.sdk.core.model.CustomerPlayerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.net.toUri
import com.evertschavez.livestreamequis.player.core.BuildConfig
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.muxstats.monitorWithMuxData

@UnstableApi
class ExoPlayerController(private val context: Context) : VideoPlayerController {
    private val adsLoader = ImaAdsLoader.Builder(context).build()
    private var muxStats: MuxStatsSdkMedia3<ExoPlayer>? = null
    private val player: ExoPlayer = ExoPlayerFactory.create(
        context = context,
        lowLatency = true,
        adsLoaderProvider = { adsLoader },
    )
    private val metricsTracker = PlayerMetricsTracker(player)
    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)

    override val state: StateFlow<PlayerState>
        get() = _state

    override val metrics: StateFlow<PlaybackMetrics>
        get() = metricsTracker.metrics

    init {
        adsLoader.setPlayer(player)
        player.addAnalyticsListener(metricsTracker)
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.value = PlayerState.Error(message = error.message ?: "Error")
                recoverIfBehindLiveWindow(error)
            }
        })
    }

    override fun prepare(config: StreamConfig) {
        val liveConfiguration =
            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.05f) // sweet spot
                .setMinPlaybackSpeed(0.95f).build()
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(config.url)
            .setLiveConfiguration(liveConfiguration)

        metricsTracker.onLoadStarted()

        config.adTagUrl?.let {
            mediaItemBuilder.setAdsConfiguration(
                MediaItem.AdsConfiguration.Builder(it.toUri()).build()
            )
        }

        monitor()

        player.setMediaItem(mediaItemBuilder.build())
        player.prepare()
    }

    override fun play() {
        player.playWhenReady = true
    }

    override fun pause() {
        player.pause()
    }

    override fun release() {
        muxStats?.release()
        player.release()
        adsLoader.release()
    }

    override fun getPlayer(): Player = player

    private fun recoverIfBehindLiveWindow(error: PlaybackException) {
        if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
            player.seekToDefaultPosition()
            player.prepare()
        }
    }

    fun monitor() {
        val customerPlayerData = CustomerPlayerData().apply {
            environmentKey = BuildConfig.MUX_ENV_KEY
        }
        val customerVideoData = CustomerVideoData().apply {
            videoTitle = "My Live Stream"
        }
        val customerData = CustomerData(customerPlayerData, customerVideoData, null)
        muxStats = player.monitorWithMuxData(
            context = context,
            envKey = BuildConfig.MUX_ENV_KEY,
            customerData = customerData
        )
    }

    private fun updateState() {
        _state.value = when (player.playbackState) {
            Player.STATE_BUFFERING -> PlayerState.Buffering
            Player.STATE_ENDED -> PlayerState.Ended
            Player.STATE_READY -> {
                if (player.isPlaying) PlayerState.Playing else PlayerState.Paused
            }
            else -> PlayerState.Idle
        }
    }
}