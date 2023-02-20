package com.zktony.www.data.remote.adapter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author: 刘贺贺
 * @date: 2023-02-20 9:33
 */

@ExperimentalCoroutinesApi
internal fun <R> ProducerScope<Any>.callEnqueueFlow(call: Call<R>) {
    call.enqueue(object : Callback<R> {
        override fun onResponse(call: Call<R>, response: Response<R>) {
            processing(response)
        }

        override fun onFailure(call: Call<R>, throwable: Throwable) {
            cancel(CancellationException(throwable.localizedMessage, throwable))
        }
    })
}

@ExperimentalCoroutinesApi
internal fun <R> ProducerScope<Any>.callExecuteFlow(call: Call<R>) {
    try {
        processing(call.execute())
    } catch (throwable: Throwable) {
        cancel(CancellationException(throwable.localizedMessage, throwable))
    }
}

@ExperimentalCoroutinesApi
internal fun <R> ProducerScope<Any>.processing(response: Response<R>) {
    if (response.isSuccessful) {
        if (response.code() != 200) {
            cancel(CancellationException("HTTP status code: ${response.code()}"))
        } else {
            trySendBlocking(response)
                .onSuccess {
                    close()
                }
                .onClosed { throwable ->
                    cancel(
                        CancellationException(
                            throwable?.localizedMessage,
                            throwable
                        )
                    )
                }
                .onFailure { throwable ->
                    cancel(
                        CancellationException(
                            throwable?.localizedMessage,
                            throwable
                        )
                    )
                }
        }
    } else {
        val msg = response.errorBody()?.string()
        cancel(
            CancellationException(
                if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                } ?: "unknown error"
            )
        )
    }
}