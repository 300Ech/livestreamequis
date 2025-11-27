package com.evertschavez.livestreamequis.player.core.controller

import com.evertschavez.livestreamequis.player.domain.controller.PlayerController

interface VideoPlayerController : PlayerController {
    fun getPlayer(): androidx.media3.common.Player
}