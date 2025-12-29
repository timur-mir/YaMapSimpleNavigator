package com.example.location.data.roomrepo

import android.content.Context
import com.example.location.domain.Mark
import com.example.location.presentation.mapToMark
import com.example.location.presentation.mapToMarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File


class MarksRepository(context:Context) {
    private val marksDao = MarksDatabaseImpl.INSTANCE!!.getMarksDao()
    private val filesDir=context.applicationContext.filesDir

    fun getPhotoFile(markPhoto: Mark): File = File(filesDir, markPhoto.photoFileName)

    fun getMarks(): Flow<List<Mark>?> {
        val allMarks = marksDao.getAllMarks()
        return allMarks.map { list -> list.map {mark -> mark.mapToMark() } }
    }
    suspend fun insertMark(mark:Mark){
        marksDao.insertMark(mark.mapToMarkEntity())
    }
    suspend fun deleteMark(id:Int){
        marksDao.deleteMark(id)
    }
    suspend fun deleteMarks(){
        marksDao.removeAllMarks()
    }
}