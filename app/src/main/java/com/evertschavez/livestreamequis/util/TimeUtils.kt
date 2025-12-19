package com.evertschavez.livestreamequis.util

import java.util.Locale

fun Long.formatDuration(): String {
    if (this < 0) return "--:--"
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}