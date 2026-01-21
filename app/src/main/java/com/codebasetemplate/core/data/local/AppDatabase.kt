package com.codebasetemplate.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codebasetemplate.shared.frame.data.local.dao.FrameDao
import com.codebasetemplate.shared.frame.data.local.entity.FrameEntity


@Database(
    entities = [FrameEntity::class],
    version = 1,
    exportSchema = true
)

abstract class AppDatabase: RoomDatabase() {
    abstract fun frameDao(): FrameDao
}