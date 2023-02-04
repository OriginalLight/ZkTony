package com.zktony.www.common.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import java.util.concurrent.ConcurrentHashMap

object DataStoreFactory {

    private const val USER_PREFERENCES = "zktony_android_preferences"

    private lateinit var defaultDataStore: DataStore<Preferences>

    private val dataStoreMaps = ConcurrentHashMap<String, DataStore<Preferences>>()

    fun init(appContext: Context) {
        getDefaultPreferencesDataStore(appContext)
    }

    private fun getDefaultPreferencesDataStore(appContext: Context): DataStore<Preferences> {
        if (this::defaultDataStore.isInitialized.not()) {
            defaultDataStore = createPreferencesDataStore(appContext, USER_PREFERENCES)
        }
        return defaultDataStore
    }

    fun getDefaultPreferencesDataStore() = defaultDataStore

    fun getPreferencesDataStore(appContext: Context, name: String): DataStore<Preferences> =
        dataStoreMaps.getOrPut(name) {
            createPreferencesDataStore(appContext, name)
        }

    private fun createPreferencesDataStore(
        appContext: Context,
        name: String
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(
                SharedPreferencesMigration(
                    appContext,
                    name
                )
            ),
            produceFile = { appContext.preferencesDataStoreFile(name) }
        )
    }

}