package com.jaylangkung.ikaspensa.utils.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LoggerDao {
    @Query("SELECT * FROM Logger")
    fun getAllLog(): List<Logger>

    @Insert
    fun insert(vararg books: Logger)
}