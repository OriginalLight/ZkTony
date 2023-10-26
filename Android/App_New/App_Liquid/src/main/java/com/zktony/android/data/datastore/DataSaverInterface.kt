@file:Suppress("UNCHECKED_CAST", "UNUSED")

package com.zktony.android.data.datastore

/**
 * @author 刘贺贺
 * @date 2023/7/24 10:59
 */
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * The interface is used to save/read data. We provide the basic implementation using Preference
 *
 * If you want to write your own, you need to implement `saveData` and `readData`. Besides, a suspend function `saveDataAsync` is optional(which is equal to `saveData` by default)
 */
abstract class DataSaverInterface(val senseExternalDataChange: Boolean = false) {
    abstract fun <T> saveData(key: String, data: T)
    abstract fun <T> readData(key: String, default: T): T
    open suspend fun <T> saveDataAsync(key: String, data: T) = saveData(key, data)
    abstract fun remove(key: String)
    abstract fun contains(key: String): Boolean

    var externalDataChangedFlow: MutableSharedFlow<Pair<String, Any?>>? =
        if (senseExternalDataChange) MutableSharedFlow(replay = 1) else null
}

/**
 * Using [HashMap] to save data in memory, can be used for testing
 * @property map MutableMap<String, Any?>
 */
class DataSaverInMemory(senseExternalDataChange: Boolean = false) :
    DataSaverInterface(senseExternalDataChange) {
    inner class ObservableMap {
        private val map by lazy {
            mutableMapOf<String, Any?>()
        }

        operator fun set(key: String, value: Any?) {
            map[key] = value
            externalDataChangedFlow?.tryEmit(key to value)
        }

        operator fun get(key: String): Any? {
            return map[key]
        }

        fun remove(key: String) {
            map.remove(key)
            externalDataChangedFlow?.tryEmit(key to null)
        }

        fun containsKey(key: String) = map.containsKey(key)
    }

    private val map = ObservableMap()

    override fun <T> saveData(key: String, data: T) {
        if (data == null) {
            remove(key)
            return
        }
        map[key] = data
    }

    override fun <T> readData(key: String, default: T): T {
        val res = map[key] ?: default
        return res as T
    }

    override fun remove(key: String) {
        map.remove(key)
    }

    override fun contains(key: String) = map.containsKey(key)

}

/**
 * You can call `LocalDataSaver.current` inside a [androidx.compose.runtime.Composable] to
 * get the instance you've provided. You can call `readData` and `saveData` then.
 */
var LocalDataSaver: ProvidableCompositionLocal<DataSaverInterface> = staticCompositionLocalOf {
    DefaultDataSaverInMemory
}

internal val DefaultDataSaverInMemory by lazy {
    DataSaverInMemory()
}

/**
 * Get the [DataSaverInterface] instance
 * which supports preview in Android Studio
 * @return DataSaverInterface
 */
@Composable
@ReadOnlyComposable
fun getLocalDataSaverInterface() =
    if (LocalInspectionMode.current) DefaultDataSaverInMemory else LocalDataSaver.current