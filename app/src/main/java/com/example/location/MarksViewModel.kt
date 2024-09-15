package com.example.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.location.data.roomrepo.MarksRepository
import com.example.location.domain.Mark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarksViewModel:ViewModel() {
    private val marksRepo = MarksRepository()
    private val _marks: MutableStateFlow<List<Mark>?> = MutableStateFlow(emptyList())
    val marks: StateFlow<List<Mark>?>
        get() = _marks.asStateFlow()
    init {
        viewModelScope.launch {
            marksRepo.getMarks().collect {
                _marks.value = it
            }
        }
    }
     fun addMark(mark:Mark) {
         viewModelScope.launch {
             marksRepo.insertMark(mark)
         }
     }
         fun getAllMarks() {
            viewModelScope.launch {
                marksRepo.getMarks().collect {
                    _marks.value = it
                }
            }
        }
         fun deleteMark(mark: Mark){
             viewModelScope.launch {
                 marksRepo.deleteMark(mark.mapToMarkEntity())
             }
         }
    fun deleteMarks(){
        viewModelScope.launch {
            marksRepo.deleteMarks()
        }
    }
    }
