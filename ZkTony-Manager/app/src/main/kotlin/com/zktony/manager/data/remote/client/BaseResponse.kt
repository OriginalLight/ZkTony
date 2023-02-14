package com.zktony.manager.data.remote.client

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 14:15
 */
class BaseResponse<T> {
    var code: Int = 0
    var msg: String? = null
    var data: T? = null
}