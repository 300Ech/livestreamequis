package com.evertschavez.livestreamequis.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.evertschavez.livestreamequis.player.core.controller.VideoPlayerController
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.evertschavez.livestreamequis.player.domain.model.StreamConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@UnstableApi
class PlayerViewModel(private val controller: VideoPlayerController) : ViewModel() {
    val player = controller.getPlayer()
    val metrics: StateFlow<PlaybackMetrics> = controller.metrics

    val playerState: StateFlow<PlayerState> = controller.state

    fun startPlayback(url: String, adTag: String?) {
        viewModelScope.launch {
            controller.prepare(
                StreamConfig(url = url, adTagUrl = adTag)
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