package com.s1g1.tomatoro.ui.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.tags.Tag
import com.s1g1.tomatoro.database.tags.TagRepository
import com.s1g1.tomatoro.service.TimerService
import com.s1g1.tomatoro.service.TimerService.Companion.DURATION_EXTRA
import com.s1g1.tomatoro.service.TimerService.Companion.MODE_EXTRA
import com.s1g1.tomatoro.service.TimerService.Companion.TAG_ID_EXTRA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(
    private val tagRepository: TagRepository,
    private val application: Application,
) : ViewModel(){

    val allUnhiddenTags: StateFlow<List<Tag>> = tagRepository.allUnhiddenTags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val secondsLeft = TimerService.secondsLeft
    val isRunning = TimerService.isRunning
    val currentFullSeconds = TimerService.currentFullSeconds

    fun addNewTag(newTagTitle: String){
        viewModelScope.launch(Dispatchers.IO) {
            tagRepository.saveTag( tag = Tag( title = newTagTitle, isRemovable = true) )
        }
    }

    fun deleteTagById(tagId: Int){
        viewModelScope.launch(Dispatchers.IO) {
            tagRepository.deleteTagById(tagId = tagId)
        }
    }

    fun onAction(
        action: TimerAction,
        durationSeconds: Long? = null,
        mode: TimerMode? = null,
        tadId: Int = 0,
    ){

        val intent = Intent(application, TimerService::class.java).apply{
            this.action = action.name
            durationSeconds?.let { putExtra(DURATION_EXTRA, it) }
            mode?.let { putExtra(MODE_EXTRA, it.name) }
            putExtra(TAG_ID_EXTRA, tadId)
        }
        application.startService(intent)
    }
}