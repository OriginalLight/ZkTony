package com.zktony.www.common.http.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.zktony.www.common.http.adapter.NetworkResponse
import okhttp3.ResponseBody
import retrofit2.Converter

class GsonResponseBodyConverter<T : Any>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>
) : Converter<ResponseBody, NetworkResponse<T>> {

    override fun convert(value: ResponseBody): NetworkResponse<T> {
        val jsonReader = gson.newJsonReader(value.charStream())
        value.use {
            jsonReader.beginObject()
            var code = 200
            var msg = ""
            var data: T? = null
            while (jsonReader.hasNext()) {
                when (jsonReader.nextName()) {
                    "code" -> code = jsonReader.nextInt()
                    "msg" -> msg = jsonReader.nextString()
                    "data" -> data = adapter.read(jsonReader)
                    else -> jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
            return if (code != 200) {
                NetworkResponse.BizError(code, msg)
            } else if (data == null) {
                /**
                 * 由于接口会有"data":null的情况，这里兜底替换为Any()，保证Success里data的非空性
                 */
                @Suppress("UNCHECKED_CAST")
                NetworkResponse.Success(Any()) as NetworkResponse<T>
            } else {
                NetworkResponse.Success(data)
            }
        }
    }
}