package com.brohit.weatherapp.data.data_source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_value_table")
data class KeyValueEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
