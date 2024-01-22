package com.zktony.android.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.zktony.android.data.entities.Setting
import kotlinx.coroutines.flow.Flow

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:49
 */
@Dao
abstract class SettingDao : BaseDao<Setting> {

    @Query(
        """
        SELECT * FROM setting
        ORDER BY id DESC
        """
    )
    abstract fun getByPage(): PagingSource<Int, Setting>

    @Query(
        """
        SELECT * FROM setting
        WHERE id = :id
        """
    )
    abstract fun getById(id: Long): Flow<Setting?>

    @Query(
        """
        DELETE FROM setting
        WHERE id = :id
        """
    )
    abstract suspend fun deleteById(id: Long)
}