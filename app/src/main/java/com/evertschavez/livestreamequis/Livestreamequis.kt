package com.evertschavez.livestreamequis

import android.app.Application
import com.evertschavez.livestreamequis.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class Livestreamequis : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Livestreamequis)
            modules(appModule)
        }
    }
}