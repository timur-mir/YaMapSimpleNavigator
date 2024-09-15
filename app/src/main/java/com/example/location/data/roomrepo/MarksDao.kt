package com.example.location.data.roomrepo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(cityInfoWeather: MarkEntity)

    @Query("SELECT * FROM marksTable ")
    fun getAllMarks(): Flow<List<MarkEntity>>

    @Query("DELETE FROM marksTable WHERE id=:id")
    suspend fun deleteMark(id: Int)

    @Query("DELETE FROM marksTable")
    suspend fun removeAllMarks()

}