package com.evertschavez.livestreamequis.player.domain.controller

import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.evertschavez.livestreamequis.player.domain.model.StreamConfig
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerState>
    val metrics: StateFlow<PlaybackMetrics>
    fun prepare(config: StreamConfig)
    fun play()
    fun pause()
    fun release()
}