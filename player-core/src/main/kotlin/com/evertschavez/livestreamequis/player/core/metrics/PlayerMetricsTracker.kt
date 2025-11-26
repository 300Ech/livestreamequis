package com.evertschavez.livestreamequis.player.core.metrics

import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@UnstableApi
class PlayerMetricsTracker(private val player: ExoPlayer) : AnalyticsListener {
    private val _metrics = MutableStateFlow(PlaybackMetrics())
    val metrics: StateFlow<PlaybackMetrics> = _metrics
    private var startTimeMs: Long = 0

    private var rebufferCount = 0

    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        val bitrate = format.bitrate.takeIf { it > 0 } ?: 0
        update(bitrateKbps = bitrate / 1000L)
    }

    override fun onIsPlayingChanged(
        eventTime: AnalyticsListener.EventTime,
        isPlaying: Boolean
    ) {
        update(isPlaying = isPlaying)
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_READY && startTimeMs > 0) {
            val startupTime = System.currentTimeMillis() - startTimeMs
            // measure and report player startup time once playback is ready
            println("PLAYER_METRICS: startup time: ${startupTime}ms")
            startTimeMs = 0
        }
    }

    private fun update(
        isPlaying: Boolean = _metrics.value.isPlaying,
        bitrateKbps: Long = _metrics.value.bitrateKbps,
    ) {
        _metrics.value = PlaybackMetrics(
            isPlaying = isPlaying,
            rebufferCount = rebufferCount,
            bitrateKbps = bitrateKbps,
        )
    }

    fun onLoadStarted() {
        startTimeMs = System.currentTimeMillis()
    }
}