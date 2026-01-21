package com.codebasetemplate.shared.frame.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.codebasetemplate.core.data.local.BaseDao
import com.codebasetemplate.shared.frame.data.local.entity.FrameEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FrameDao : BaseDao<FrameEntity> {

    @Query("SELECT * FROM frame_table ORDER BY date_modifier ASC")
    fun getFrameItems(): Flow<List<FrameEntity>>

    @Query("DELETE FROM frame_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM frame_table WHERE id = :id")
    fun getFrameById(id: String): FrameEntity?
}