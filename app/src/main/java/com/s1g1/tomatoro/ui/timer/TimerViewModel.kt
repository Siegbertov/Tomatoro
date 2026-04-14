package com.s1g1.tomatoro.ui.timer

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.service.TimerService
import com.s1g1.tomatoro.service.TimerService.Companion.DURATION_EXTRA
import com.s1g1.tomatoro.service.TimerService.Companion.MODE_EXTRA

class TimerViewModel(
    private val application: Application,
) : ViewModel(){
    val secondsLeft = TimerService.secondsLeft
    val isRunning = TimerService.isRunning
    val currentFullSeconds = TimerService.currentFullSeconds

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
}