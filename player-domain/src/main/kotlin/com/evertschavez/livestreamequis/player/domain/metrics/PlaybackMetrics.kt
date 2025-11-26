package com.evertschavez.livestreamequis.player.domain.metrics

data class PlaybackMetrics(
    val isPlaying: Boolean = false,
    val rebufferCount: Int = 0,
    val bitrateKbps: Long = 0,
)