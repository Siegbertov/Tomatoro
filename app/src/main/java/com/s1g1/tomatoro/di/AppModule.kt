package com.s1g1.tomatoro.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.s1g1.tomatoro.SettingsRepository
import com.s1g1.tomatoro.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single{
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile("settings") }
        )
    }

    single { SettingsRepository(get()) }

    viewModel { SettingsViewModel(get()) }
}