package com.evertschavez.livestreamequis.di

import android.annotation.SuppressLint
import com.evertschavez.livestreamequis.player.core.controller.ExoPlayerController
import com.evertschavez.livestreamequis.player.core.controller.VideoPlayerController
import com.evertschavez.livestreamequis.ui.player.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@SuppressLint("UnsafeOptInUsageError")
val appModule = module {
    factory<VideoPlayerController> {
        ExoPlayerController(androidContext())
    }

    viewModel {
        PlayerViewModel(get())
    }
}