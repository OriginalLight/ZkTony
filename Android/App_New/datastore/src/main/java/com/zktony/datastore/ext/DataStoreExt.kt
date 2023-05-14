package com.zktony.datastore.ext

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.zktony.datastore.DataStoreFactory
import com.zktony.datastore.SettingsPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

private val defaultDataStore: DataStore<Preferences> = DataStoreFactory.default()
private val settingsDataStore: DataStore<SettingsPreferences> = DataStoreFactory.settings()

@Suppress("UNCHECKED_CAST")
@OptIn(DelicateCoroutinesApi::class)
fun DataStore<Preferences>.save(key: String, value: Any) {
    GlobalScope.launch {
        when (value) {
            is String -> {
                edit { preferences ->
                    preferences[stringPreferencesKey(key)] = value
                }
            }

            is Boolean -> {
                edit { preferences ->
                    preferences[booleanPreferencesKey(key)] = value
                }
            }

            is Int -> {
                edit { preferences ->
                    preferences[intPreferencesKey(key)] = value
                }
            }

            is Long -> {
                edit { preferences ->
                    preferences[longPreferencesKey(key)] = value
                }
            }

            is Float -> {
                edit { preferences ->
                    preferences[floatPreferencesKey(key)] = value
                }
            }

            is Double -> {
                edit { preferences ->
                    preferences[doublePreferencesKey(key)] = value
                }
            }

            is Set<*> -> {
                edit { preferences ->
                    preferences[stringSetPreferencesKey(key)] = value as Set<String>
                }
            }

            else -> throw IllegalArgumentException("This type can be saved into DataStore")
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> DataStore<Preferences>.read(key: String, defaultValue: T): Flow<T> {
    return data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        when (T::class) {
            String::class -> preferences[stringPreferencesKey(key)] ?: defaultValue
            Boolean::class -> preferences[booleanPreferencesKey(key)] ?: defaultValue
            Int::class -> preferences[intPreferencesKey(key)] ?: defaultValue
            Long::class -> preferences[longPreferencesKey(key)] ?: defaultValue
            Float::class -> preferences[floatPreferencesKey(key)] ?: defaultValue
            Double::class -> preferences[doublePreferencesKey(key)] ?: defaultValue
            Set::class -> preferences[stringSetPreferencesKey(key)] ?: defaultValue
            else -> throw IllegalArgumentException("This type can be saved into DataStore")
        }
    } as Flow<T>
}


val settingsFlow: Flow<SettingsPreferences> = settingsDataStore.data.catch { exception ->
    if (exception is IOException) {
        emit(SettingsPreferences.getDefaultInstance())
    } else {
        throw exception
    }
}

val settings: SettingsPreferences = runBlocking { settingsDataStore.data.first() }

suspend fun saveSettings(block: suspend (SettingsPreferences) -> SettingsPreferences) {
    settingsDataStore.updateData(block)
}

fun read(key: String, defaultValue: Any): Flow<Any> {
    return defaultDataStore.read(key, defaultValue)
}

fun save(key: String, value: Any) {
    defaultDataStore.save(key, value)
}