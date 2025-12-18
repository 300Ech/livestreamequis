package com.evertschavez.livestreamequis.player.domain.metrics

data class PlaybackMetrics(
    val isPlaying: Boolean = false,
    val rebufferCount: Int = 0,
    val bitrateKbps: Long = 0,
    val resolution: String = "N/A",
    val videoCodec: String = "N/A",
    val audioCodec: String = "N/A",
    val videoFormat: String = "N/A",
)