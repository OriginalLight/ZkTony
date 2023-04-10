package com.zktony.common.ext

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

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
