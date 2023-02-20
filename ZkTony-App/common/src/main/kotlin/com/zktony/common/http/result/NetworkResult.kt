package com.zktony.common.http.result

import com.zktony.common.http.result.NetworkResult.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Response

sealed class NetworkResult<out R> {

    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val throwable: Throwable?) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[throwable=$throwable]"
            Loading -> "Loading"
        }
    }
}

val NetworkResult<*>.succeeded
    get() = this is Success && data != null

fun <T> NetworkResult<T>.successOr(fallback: T): T {
    return (this as? Success<T>)?.data ?: fallback
}

val <T> NetworkResult<T>.data: T?
    get() = (this as? Success)?.data

inline fun <reified T> NetworkResult<T>.updateOnSuccess(stateFlow: MutableStateFlow<T>) {
    if (this is Success) {
        stateFlow.value = data
    }
}

fun <T> Flow<Response<T>>.getNetworkResult() = flow {
    emit(NetworkResult.Loading)
    this@getNetworkResult
        .flowOn(Dispatchers.IO)
        .catch { emit(NetworkResult.Error(it)) }
        .collect {
            val body = it.body()
            if (body != null) {
                emit(Success(body))
            } else {
                emit(Success(null))
            }
        }
}
