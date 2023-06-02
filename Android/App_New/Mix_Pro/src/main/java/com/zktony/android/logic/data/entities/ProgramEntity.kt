package com.zktony.android.logic.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.google.errorprone.annotations.Immutable
import com.zktony.android.logic.data.FloatConverters
import com.zktony.android.logic.data.IntConverters
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(
    tableName = "programs",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@Immutable
@TypeConverters(
    IntConverters::class,
    FloatConverters::class,
)
data class ProgramEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "sub_id") val subId: Long = 0L,
    @ColumnInfo(name = "text") val text: String = "None",
    @ColumnInfo(name = "active") val active: List<Int> = listOf(0, 1, 2, 3, 4, 5),
    @ColumnInfo(name = "volume") val volume: List<Float> = listOf(0f, 0f, 0f, 0f),
    @ColumnInfo(name = "count") val count: Int = 0,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
)

data class PWC(
    @Embedded
    val program: ProgramEntity,
    @Relation(
        parentColumn = "sub_id",
        entityColumn = "id",
        entity = ContainerEntity::class,
    )
    val container: ContainerEntity,
)