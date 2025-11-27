package com.evertschavez.livestreamequis.player.domain.model

data class VideoItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val streamUrl: String,
    val adTagUrl: String? = null,
    val thumbnailUrl: String = "",
)