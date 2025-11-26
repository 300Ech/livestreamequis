package com.evertschavez.livestreamequis.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evertschavez.livestreamequis.player.core.controller.ExoPlayerController
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.evertschavez.livestreamequis.player.domain.model.StreamConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application = application) {
    private val controller = ExoPlayerController(application.applicationContext)

    val player = controller.getPlayer()
    val metrics: StateFlow<PlaybackMetrics> = controller.metrics

    val playerState: StateFlow<PlayerState> = controller.state

    fun startPlayback() {
        viewModelScope.launch {
            controller.prepare(
                StreamConfig(url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
            )
            controller.play()
        }
    }

    fun pausePlayback() {
        controller.pause()
    }

    fun resumePlayback() {
        controller.play()
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}