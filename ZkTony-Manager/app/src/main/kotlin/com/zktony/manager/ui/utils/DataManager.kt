package com.zktony.manager.ui.utils

import android.content.Context
import androidx.room.Room
import com.zktony.manager.BuildConfig
import com.zktony.manager.data.local.room.AppDatabase
import com.zktony.manager.data.remote.service.ApplicationService
import com.zktony.manager.data.remote.service.SoftwareService
import com.zktony.manager.data.store.ApplicationStore
import com.zktony.manager.data.store.SoftwareStore
import com.zktony.manager.data.store.UserStore
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.annotation.DefaultDomain
import rxhttp.wrapper.cache.CacheMode
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author: 刘贺贺
 * @date: 2023-02-16 10:00
 */
object DataManager {

    private lateinit var database: AppDatabase

    val applicationStore by lazy { ApplicationStore(
        service = ApplicationService()
    ) }

    val softwareStore by lazy { SoftwareStore(
        service = SoftwareService()
    ) }

    val userStore by lazy { UserStore(
        dao = database.userDao()
    ) }



    @DefaultDomain
    const val BASE_URL = "http://192.168.10.103:9765"

    fun provide(context: Context) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val cacheDir = File(context.cacheDir, "http_cache")

        RxHttpPlugins.init(okHttpClient)
            .setDebug(BuildConfig.DEBUG, false, 2)
            .setCache(cacheDir, 10 * 1024 * 1024, CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE, 60 * 1000)

        database = Room.databaseBuilder(context, AppDatabase::class.java, "data.db")
            // This is not recommended for normal apps, but the goal of this sample isn't to
            // showcase all of Room.
            .fallbackToDestructiveMigration()
            .build()
    }

}