package com.zktony.manager.data.remote.adapter

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author: 刘贺贺
 * @date: 2023-02-20 9:34
 */
class FlowCallAdapterFactory private constructor(private var isAsync: Boolean) :
    CallAdapter.Factory() {

    companion object {
        /**
         * 同步
         */
        fun create(): FlowCallAdapterFactory =
            FlowCallAdapterFactory(false)

        /**
         * 异步
         */
        fun createAsync(): FlowCallAdapterFactory =
            FlowCallAdapterFactory(true)
    }

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) {
            return null
        }
        val observableType = getParameterUpperBound(
            0,
            getParameterUpperBound(0, returnType as ParameterizedType) as ParameterizedType
        )
        return FlowCallAdapter<Any>(observableType, isAsync)
    }

}