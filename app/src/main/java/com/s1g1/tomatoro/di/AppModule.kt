package com.s1g1.tomatoro.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.s1g1.tomatoro.SettingsRepository
import com.s1g1.tomatoro.ui.settings.SettingsViewModel
import com.s1g1.tomatoro.database.AppDatabase
import com.s1g1.tomatoro.database.SessionRepository
import com.s1g1.tomatoro.ui.stats.StatsViewModel
import com.s1g1.tomatoro.ui.timer.TimerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // PREFERENCES COMPONENT
    single{
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("settings") }
        )
    }

    single { SettingsRepository(get()) }

    viewModel { SettingsViewModel(get()) }

    // APP DATABASE COMPONENT
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    single { get<AppDatabase>().sessionDao() }

    single { SessionRepository(get()) }

    viewModel { TimerViewModel(
        sessionRepository = get(),
        application = androidApplication()
        )
    }

    // STATS COMPONENT
    viewModel { StatsViewModel(get()) }
}