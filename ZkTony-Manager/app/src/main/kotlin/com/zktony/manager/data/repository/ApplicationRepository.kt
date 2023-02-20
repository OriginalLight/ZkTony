/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zktony.manager.data.repository

import com.zktony.manager.data.remote.result.NetworkResult
import com.zktony.manager.data.remote.service.ApplicationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ApplicationRepository @Inject constructor(
    private val service: ApplicationService
) {
    fun getApplicationById(id: String = "com.zktony.www.zm.debug") = flow {
        emit(NetworkResult.Loading)
        service.getById(id)
            .flowOn(Dispatchers.IO)
            .catch { e ->
                emit(NetworkResult.Error(e))
            }.collect {
                val body = it.body()
                if (body != null) {
                    emit(NetworkResult.Success(body))
                } else {
                    emit(NetworkResult.Success(null))
                }
            }
    }
}
