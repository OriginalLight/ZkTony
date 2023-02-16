package com.zktony.manager.data.remote.client

import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 14:16
 */
@Parser(name = "Response")
open class ResponseParser<T> : TypeParser<T> {

    //以下两个构造方法是必须的
    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        val data: BaseResponse<T> = response.convertTo(BaseResponse::class, *types)
        var t = data.data     //获取data字段

        if (t == null && String::class.java == types[0]) {
            //判断我们传入的泛型是String对象，就给t赋值""字符串，确保t不为null
            t = "" as T
        }
        if (data.code != 200 || t == null) { //code不等于200，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return t  //最后返回data字段
    }
}