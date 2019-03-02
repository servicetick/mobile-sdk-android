package com.servicetick.android.library.entities.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
internal class Statistic(@PrimaryKey val key: String, val value: String)