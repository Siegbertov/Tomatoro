package com.s1g1.tomatoro.ui.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.Session
import com.s1g1.tomatoro.database.SessionRepository
import com.s1g1.tomatoro.service.TimerService
import com.s1g1.tomatoro.service.TimerService.Companion.DURATION_EXTRA
import com.s1g1.tomatoro.service.TimerService.Companion.MODE_EXTRA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerViewModel(
    private val sessionRepository: SessionRepository,
    private val application: Application,
) : ViewModel(){
    val secondsLeft = TimerService.secondsLeft
    val isRunning = TimerService.isRunning
    val currentFullSeconds = TimerService.currentFullSeconds

    val currentModeName = TimerService.currentModeName


    fun onAction(
        action: TimerAction,
        durationSeconds: Long? = null,
        mode: TimerMode? = null,
    ){

        val intent = Intent(application, TimerService::class.java).apply{
            this.action = action.name
            durationSeconds?.let { putExtra(DURATION_EXTRA, it) }
            mode?.let { putExtra(MODE_EXTRA, it.name) }
        }
        application.startService(intent)
    }

    private fun saveSessionToDatabase(session: Session){
        viewModelScope.launch(Dispatchers.IO){
            sessionRepository.saveSession(session = session)
        }
    }
    fun deleteSessionFromDatabase(session: Session){
        viewModelScope.launch(Dispatchers.IO){
            sessionRepository.deleteSession(session = session)
        }
    }
}