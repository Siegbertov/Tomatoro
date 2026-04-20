package com.s1g1.tomatoro.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.s1g1.tomatoro.SettingsRepository
import com.s1g1.tomatoro.ui.settings.SettingsViewModel
import com.s1g1.tomatoro.database.AppDatabase
import com.s1g1.tomatoro.database.MIGRATION_1_2
import com.s1g1.tomatoro.database.MIGRATION_2_3
import com.s1g1.tomatoro.database.TAGS_PREFILL_CALLBACK
import com.s1g1.tomatoro.database.sessions.SessionRepository
import com.s1g1.tomatoro.database.tags.TagRepository
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

    viewModel { SettingsViewModel(
        repository = get(),
        application = androidApplication()
    ) }

    // APP DATABASE COMPONENT
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3
            )
            .addCallback(TAGS_PREFILL_CALLBACK)
            .build()
    }

    // session table
    single { get<AppDatabase>().sessionDao() }
    single { SessionRepository(sessionDao = get()) }

    // tag table
    single { get<AppDatabase>().tagDao() }
    single { TagRepository(tagDao = get()) }

    viewModel { TimerViewModel(
        application = androidApplication()
        )
    }

    // STATS COMPONENT
    viewModel { StatsViewModel(get()) }
}