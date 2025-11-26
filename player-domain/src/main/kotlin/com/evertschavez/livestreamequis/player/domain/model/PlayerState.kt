package com.evertschavez.livestreamequis.player.domain.model

sealed class PlayerState {
    object Idle: PlayerState()
    object Buffering: PlayerState()
    object Playing: PlayerState()
    object Paused: PlayerState()
    data class Error(val message: String): PlayerState()
}