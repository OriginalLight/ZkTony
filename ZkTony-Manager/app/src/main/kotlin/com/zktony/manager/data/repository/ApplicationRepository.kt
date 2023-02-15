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

import android.content.Context
import com.zktony.manager.BuildConfig
import com.zktony.manager.data.model.Application
import com.zktony.manager.data.remote.client.NetworkResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * An Interface contract to get all accounts info for User.
 */
interface ApplicationRepository {
    fun getApplicationById(id: String = BuildConfig.APPLICATION_ID): Flow<NetworkResult<Application>>
    fun downloadApplication(context: Context, url: String): Flow<NetworkResult<File>>
}
