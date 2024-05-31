package com.brohit.weatherapp.data.data_source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brohit.weatherapp.data.data_source.local.entity.KeyValueEntity

@Dao
interface KeyValueDao {
    @Query("SELECT value FROM key_value_table WHERE `key` = :key")
    fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(vararg keyValueEntity: KeyValueEntity)

    @Query("DELETE FROM key_value_table")
    fun clear()

    @Query("DELETE FROM key_value_table WHERE `key` = :key")
    fun remove(key: String)


}