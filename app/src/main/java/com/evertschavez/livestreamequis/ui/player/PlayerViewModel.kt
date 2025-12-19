package com.evertschavez.livestreamequis.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.evertschavez.livestreamequis.player.core.controller.VideoPlayerController
import com.evertschavez.livestreamequis.player.domain.metrics.PlaybackMetrics
import com.evertschavez.livestreamequis.player.domain.model.PlayerState
import com.evertschavez.livestreamequis.player.domain.model.StreamConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val playerState: PlayerState = PlayerState.Idle,
    val metrics: PlaybackMetrics = PlaybackMetrics(),
    val title: String = "",
    val subtitle: String = "",
    val isUiVisible: Boolean = true,
    val duration: String = "00:00",
)

@UnstableApi
class PlayerViewModel(private val controller: VideoPlayerController) : ViewModel() {
    private val _metadata = MutableStateFlow(Pair("", ""))
    private val _isUiVisible = MutableStateFlow(true)
    val player = controller.getPlayer()

    val uiState: StateFlow<PlayerUiState> = combine(
        controller.state,
        controller.metrics,
        _metadata,
        _isUiVisible
    ) { state, metrics, metadata, isUiVisible ->
        PlayerUiState(
            playerState = state,
            metrics = metrics,
            title = metadata.first,
            subtitle = metadata.second,
            isUiVisible = isUiVisible,
            duration = formatDuration(controller.getPlayer().duration),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun initialize(url: String, adTag: String?, title: String, subtitle: String) {
        _metadata.value = title to subtitle
        viewModelScope.launch {
            controller.prepare(StreamConfig(url = url, adTagUrl = adTag))
            controller.play()
        }
    }

    fun toggleUi() {
        _isUiVisible.value = !_isUiVisible.value
    }

    fun showUi() {
        _isUiVisible.value = true
    }

    fun onPlayPauseClicked() {
        if (uiState.value.playerState == PlayerState.Playing) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun stopPlayback() {
        controller.pause()
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }

    private fun formatDuration(millis: Long): String {
        if (millis < 0) return "--:--"
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}