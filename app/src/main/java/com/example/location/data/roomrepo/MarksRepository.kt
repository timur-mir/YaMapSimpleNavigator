package com.example.location.data.roomrepo

import com.example.location.domain.Mark
import com.example.location.mapToMark
import com.example.location.mapToMarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class MarksRepository {
    private val marksDao = MarksDatabaseImpl.INSTANCE!!.getMarksDao()

    fun getMarks(): Flow<List<Mark>?> {
        val allMarks = marksDao.getAllMarks()
        return allMarks.map { list -> list.map {mark -> mark.mapToMark() } }
    }
    suspend fun insertMark(mark:Mark){
        marksDao.insertMark(mark.mapToMarkEntity())
    }
    suspend fun deleteMark(mark:MarkEntity){
        marksDao.deleteMark(mark)
    }
    suspend fun deleteMarks(){
        marksDao.removeAllMarks()
    }
}