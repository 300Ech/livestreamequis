package com.evertschavez.livestreamequis.player.core.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ads.AdsLoader
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

object ExoPlayerFactory {
    @OptIn(UnstableApi::class)
    fun create(
        context: Context,
        lowLatency: Boolean,
        adsLoaderProvider: AdsLoader.Provider? = null,
    ): ExoPlayer {
        val minBufferMs = if (lowLatency) 1000 else 5000
        val maxBufferMs = if (lowLatency) 2000 else 5000
        val bufferForPlaybackMs = if (lowLatency) 500 else 2500
        val bufferForRebufferMs = if (lowLatency) 1000 else 5000
        val trackSelector = DefaultTrackSelector(context)
        trackSelector.buildUponParameters().setMaxVideoSizeSd().setForceLowestBitrate(false).build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferForRebufferMs,
            )
            .build()

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setLocalAdInsertionComponents(
                { adsLoaderProvider?.getAdsLoader(it) },
                /* adViewProvider= */ { null }
            )

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }
}