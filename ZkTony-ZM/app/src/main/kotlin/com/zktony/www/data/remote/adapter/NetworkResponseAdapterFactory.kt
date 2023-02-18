package com.zktony.www.data.remote.adapter

import com.zktony.www.common.utils.Logger
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResponseAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        // suspend 函数在 Retrofit 中的返回值其实是 `Call`
        if (Call::class.java != getRawType(returnType)) return null

        // 检查返回类型是否为 `ParameterizedType`
        check(returnType is ParameterizedType) {
            "return type must be parameterized as Call<NetworkResponse<<Foo>> or Call<NetworkResponse<out Foo>>"
        }

        // 获取Call内的一层泛型类型
        val responseType = getParameterUpperBound(0, returnType)

        // 如果非NetworkResponse不处理
        if (getRawType(responseType) != NetworkResponse::class.java) return null

        return NetworkResponseAdapter(responseType, object :
            com.zktony.www.data.remote.adapter.ErrorHandler {
            override fun bizError(code: Int, msg: String) {
                Logger.d(msg = "bizError: code:$code - msg: $msg")
            }

            override fun otherError(throwable: Throwable) {
                Logger.e(msg = throwable.message.toString(), throwable = throwable)
            }
        })
    }
}
