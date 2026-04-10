package com.s1g1.tomatoro

import android.app.Application
import com.s1g1.tomatoro.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TomatoroApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // DI EXCEPTIONS
            androidLogger()

            // PASS APP CONTEXT
            androidContext(this@TomatoroApp)

            // FUTURE MODULES
            modules(appModule)
        }
    }

}