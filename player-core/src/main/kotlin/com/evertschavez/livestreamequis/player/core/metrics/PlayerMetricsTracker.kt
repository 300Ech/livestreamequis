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
        val resolution = "${format.width}x${format.height}"
        val codec = format.sampleMimeType ?: "Unknown"

        var newBitrate = _metrics.value.bitrateKbps
        if (format.bitrate > 0) {
            newBitrate = format.bitrate / 1000L
        }

        update(
            bitrateKbps = newBitrate,
            resolution = resolution,
            videoCodec = codec,
        )
    }

    override fun onAudioInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        val audioCodec = format.sampleMimeType ?: "Unknown"
        update(audioCodec = audioCodec)
    }

    override fun onIsPlayingChanged(
        eventTime: AnalyticsListener.EventTime,
        isPlaying: Boolean
    ) {
        update(isPlaying = isPlaying)
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_BUFFERING) {
            rebufferCount++
            update()
        }

        if (state == Player.STATE_READY && startTimeMs > 0) {
            val startupTime = System.currentTimeMillis() - startTimeMs
            println("PLAYER_METRICS: startup time: ${startupTime}ms")
            startTimeMs = 0
        }
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        update(bitrateKbps = bitrateEstimate / 1000L)
    }

    private fun update(
        isPlaying: Boolean = _metrics.value.isPlaying,
        bitrateKbps: Long = _metrics.value.bitrateKbps,
        resolution: String = _metrics.value.resolution,
        videoCodec: String = _metrics.value.videoCodec,
        audioCodec: String = _metrics.value.audioCodec
    ) {
        _metrics.value = PlaybackMetrics(
            isPlaying = isPlaying,
            rebufferCount = rebufferCount,
            bitrateKbps = bitrateKbps,
            resolution = resolution,
            videoCodec = videoCodec,
            audioCodec = audioCodec,
        )
    }

    fun onLoadStarted() {
        startTimeMs = System.currentTimeMillis()
    }
}