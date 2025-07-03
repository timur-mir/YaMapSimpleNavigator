package com.example.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.location.data.roomrepo.MarksRepository
import com.example.location.domain.Mark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarksViewModel:ViewModel() {
    private val marksRepo = MarksRepository(ApplicationMapKit.applicationContext())
    private val _marks: MutableStateFlow<List<Mark>?> = MutableStateFlow(emptyList())
    val marks: StateFlow<List<Mark>?>
        get() = _marks.asStateFlow()
    private val _marks2: MutableStateFlow<List<Mark>?> = MutableStateFlow(emptyList())
    val marks2: StateFlow<List<Mark>?>
        get() = _marks2.asStateFlow()
    private val _marksSize: MutableStateFlow<Int> = MutableStateFlow(0)
    val marksSize: StateFlow<Int>
        get() = _marksSize.asStateFlow()
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
    fun getMarksSize() {
        viewModelScope.launch {
            marksRepo.getMarks().collect {
                if (it != null) {
                    _marksSize.value = it.size
                }
                else
                    _marksSize.value = 0
            }
        }
    }
         fun getAllMarks() {
            viewModelScope.launch {
                marksRepo.getMarks().collect {
                    _marks2.value = it
                }
            }
        }
         fun deleteMark(id:Int){
             viewModelScope.launch {
                 marksRepo.deleteMark(id)
             }
         }
    fun deleteMarks(){
        viewModelScope.launch {
            marksRepo.deleteMarks()
        }
    }
    }
