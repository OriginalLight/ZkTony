package com.zktony.datastore

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * A state which holds the value.  It implements the [MutableState] interface, so you can use
 * it just like a normal state. see [mutableDataSaverStateOf]
 *
 * When assign a new value to it, it will do data persistence according to [SavePolicy], which is [SavePolicy.IMMEDIATELY]
 * by default. If you want to save data manually, you can call [saveData].
 *
 * You can call `val (value, setValue) = state` to get its `set` function.
 *
 * @param T the class of data
 * @param dataSaverInterface the interface to read/save data, see [DataSaverInterface]
 * @param key persistence key
 * @param initialValue NOTE: YOU SHOULD READ THE SAVED VALUE AND PASSED IT AS THIS PARAMETER BY YOURSELF(see: [mutableDataSaverStateOf])
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async Boolean whether to save data asynchronously
 */
class DataSaverMutableState<T>(
    private val dataSaverInterface: DataSaverInterface,
    private val key: String,
    private val initialValue: T,
    private val savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    private val async: Boolean = false
) : MutableState<T> {
    private val state = mutableStateOf(initialValue)
    private var job: Job? = null
    private val scope by lazy(LazyThreadSafetyMode.PUBLICATION) {
        CoroutineScope(Dispatchers.IO)
    }

    override var value: T
        get() = state.value
        set(value) {
            doSetValue(value)
        }

    operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        doSetValue(value)
    }


    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = state.value

    /**
     * This function will convert and save current data.
     * If `async` is true, it will `launch` a coroutine
     * to do that.
     */
    fun saveData() {
        if (value == null) {
            dataSaverInterface.remove(key)
            return
        }
        val value = value!!
        if (async) {
            job?.cancel()
            job = scope.launch {
                dataSaverInterface.saveDataAsync(key, value)
            }
        } else {
            dataSaverInterface.saveData(key, value)
        }
    }

    /**
     * remove the key and set the value to `replacement`
     * @param replacement List<T> new value of the state, `initialValue` by default
     */
    fun remove(replacement: T = initialValue) {
        dataSaverInterface.remove(key)
        state.value = replacement
    }

    fun valueChangedSinceInit() = state.value != initialValue

    /**
     * set the value without saving data to disk
     * @param value T
     */
    fun setValueWithoutSave(value: T) {
        state.value = value
    }


    private fun doSetValue(value: T) {
        val oldValue = this.state.value
        this.state.value = value
        if (oldValue != value && savePolicy == SavePolicy.IMMEDIATELY)
            saveData()
    }


    override operator fun component1() = state.value

    override operator fun component2(): (T) -> Unit = ::doSetValue
}

/**
 * This function provide an elegant way to do data persistence.
 * Check the example in `README.md` to see how to use it.
 *
 * NOTE: THE VERSION WITH PARAMETER `savePolicy` IS PREFERRED.
 *
 * @param key String
 * @param default T default value if it is initialized the first time
 * @param autoSave Boolean whether to do data persistence each time you do assignment
 * @return DataSaverMutableState<T>
 */
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    default: T,
    autoSave: Boolean = true
): DataSaverMutableState<T> = rememberDataSaverState(
    key = key,
    initialValue = default,
    savePolicy = if (autoSave) SavePolicy.IMMEDIATELY else SavePolicy.NEVER
)

/**
 * This function READ AND CONVERT the saved data and return a remembered [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 * ================================
 *
 * 此函数 **读取并转换** 已保存的数据，返回remember后的 [DataSaverMutableState]
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
@Composable
inline fun <reified T> rememberDataSaverState(
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true,
    senseExternalDataChange: Boolean = false
): DataSaverMutableState<T> {
    val saverInterface = getLocalDataSaverInterface()
    var state: DataSaverMutableState<T>? = null

    LaunchedEffect(key1 = senseExternalDataChange) {
        if (!senseExternalDataChange || state == null) return@LaunchedEffect
        if (!saverInterface.senseExternalDataChange) {
            return@LaunchedEffect
        }
        saverInterface.externalDataChangedFlow?.collect { pair ->
            val (k, v) = pair
            if (k == key && v != state?.value) {
                val d = if (v != null) {
                    try {
                        v as T
                    } catch (e: Exception) {
                        throw e
                    }
                } else {
                    // if the value is null
                    // and the type is nullable
                    initialValue
                }
                // to avoid duplicate save
                state?.setValueWithoutSave(d)
            }
        }
    }

    DisposableEffect(key, savePolicy) {
        onDispose {
            if (savePolicy == SavePolicy.DISPOSED && state != null && state!!.valueChangedSinceInit()) {
                state!!.saveData()
            }
        }
    }

    return remember(saverInterface, key, async) {
        mutableDataSaverStateOf(saverInterface, key, initialValue, savePolicy, async).also {
            state = it
        }
    }
}

/**
 * This function READ AND CONVERT the saved data and return a [DataSaverMutableState].
 * Check the example in `README.md` to see how to use it.
 *
 * 此函数 **读取并转换** 已保存的数据，返回 [DataSaverMutableState]
 *
 * @param key String
 * @param initialValue T default value if it is initialized the first time
 * @param savePolicy how and when to save data, see [SavePolicy]
 * @param async  whether to save data asynchronously
 * @return DataSaverMutableState<T>
 *
 * @see DataSaverMutableState
 */
inline fun <reified T> mutableDataSaverStateOf(
    dataSaverInterface: DataSaverInterface,
    key: String,
    initialValue: T,
    savePolicy: SavePolicy = SavePolicy.IMMEDIATELY,
    async: Boolean = true
): DataSaverMutableState<T> {
    val data = try {
        if (!dataSaverInterface.contains(key)) initialValue
        else dataSaverInterface.readData(key, initialValue)
    } catch (e: Exception) {
        dataSaverInterface.readData(key, initialValue)
    }
    return DataSaverMutableState(dataSaverInterface, key, data, savePolicy, async)
}