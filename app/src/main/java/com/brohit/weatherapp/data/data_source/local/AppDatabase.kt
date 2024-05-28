package com.brohit.weatherapp.data.data_source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brohit.weatherapp.data.data_source.local.dao.KeyValueDao
import com.brohit.weatherapp.data.data_source.local.entity.KeyValueEntity


@Database(
    entities = [KeyValueEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val keyValueDao: KeyValueDao
}