package com.zktony.www.common.http

import com.zktony.www.common.app.CommonApplicationProxy
import com.zktony.www.common.extension.isNetworkAvailable
import com.zktony.www.common.extension.showShortToast
import com.zktony.www.common.http.adapter.ErrorHandler
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Error的Toast处理
 */
internal object ErrorToastHandler : ErrorHandler {

    private const val ERROR_DEFAULT = "请求失败"
    private const val ERROR_CONNECTED_TIME_OUT = "请求链接超时"
    private const val ERROR_NET_WORK_DISCONNECTED = "网络连接异常"

    private fun handle(throwable: Throwable): String =
        when (throwable) {
            is IOException -> {
                if (CommonApplicationProxy.application.isNetworkAvailable().not()) {
                    ERROR_NET_WORK_DISCONNECTED
                } else handIoException(throwable)
            }

            else -> ERROR_DEFAULT
        }

    override fun bizError(code: Int, msg: String) {
        msg.showShortToast()
    }

    override fun otherError(throwable: Throwable) {
        handle(throwable).showShortToast()
    }

    private fun handIoException(ioException: IOException): String {
        return when (ioException) {
            is SocketTimeoutException -> {
                ERROR_CONNECTED_TIME_OUT
            }

            else -> ERROR_DEFAULT
        }
    }
}