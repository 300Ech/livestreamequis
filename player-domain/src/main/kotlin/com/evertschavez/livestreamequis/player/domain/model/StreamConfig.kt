package com.evertschavez.livestreamequis.player.domain.model

data class StreamConfig(
    val url: String,
    val isLive: Boolean = false,
    val lowLatency: Boolean = true,
    val adTagUrl: String? = null,
    val title: String?,
)