package com.zktony.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.zktony.proto.SettingsPreferences

object DataStoreFactory {

    private lateinit var defaultDataStore: DataStore<Preferences>
    private lateinit var settingsDataStore: DataStore<SettingsPreferences>

    fun init(appContext: Context) {
        if (!::defaultDataStore.isInitialized) {
            defaultDataStore = createPreferencesDataStore(appContext)
        }
        if (!::settingsDataStore.isInitialized) {
            settingsDataStore = createSettingsPreferencesDataStore(appContext)
        }
    }

    fun default(): DataStore<Preferences> {
        return defaultDataStore
    }

    fun settings(): DataStore<SettingsPreferences> {
        return settingsDataStore
    }

    private fun createPreferencesDataStore(appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { appContext.preferencesDataStoreFile("default_preferences.pb") }
        )
    }

    private fun createSettingsPreferencesDataStore(appContext: Context): DataStore<SettingsPreferences> {
        return DataStoreFactory.create(
            serializer = SettingPreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { SettingsPreferences.getDefaultInstance() }
            ),
        ) {
            appContext.preferencesDataStoreFile("settings_preferences.pb")
        }
    }

}