package com.s1g1.tomatoro.ui.timer

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.Session
import com.s1g1.tomatoro.database.SessionRepository
import com.s1g1.tomatoro.triggerVibration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimerViewModel(private val sessionRepository: SessionRepository) : ViewModel(){

    private val _timeLeft = MutableStateFlow(60L * 25)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    var currentFullTime: Long? = null
    var currentMode: TimerMode? = null

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(initialSeconds: Long, context: Context, mode: TimerMode){
        if (_isRunning.value) return

        if (currentFullTime==null) {
            currentFullTime = initialSeconds
            currentMode = mode
        }
        Log.d(TAG, "STARTED at ${getCurrentFormattedTime()} - ${currentMode?.name} - $currentFullTime")

        _isRunning.value = true
        countDownTimer = object : CountDownTimer(initialSeconds*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                Log.d(TAG, "CONGRATULATION: passed ${currentMode?.name} with $currentFullTime at ${getCurrentFormattedTime()}")

                triggerVibration(context=context)

                saveSessionToDatabase(
                    Session(
                        endTimestamp = System.currentTimeMillis(),
                        mode = currentMode ?: TimerMode.getDefault(),
                        duration = currentFullTime ?: 0L
                    )
                )

                _timeLeft.value = currentFullTime ?: 0 // reset timer with same time as before
                _isRunning.value = false
                resetFullTimeAndMode()
            }
        }.start()
    }

    fun pauseTimer(callFromReset: Boolean = false){
        if (!callFromReset){
            Log.d(TAG, "PAUSED at ${getCurrentFormattedTime()} - ${currentMode?.name} - $currentFullTime")
        }
        _isRunning.value = false
        countDownTimer?.cancel()
    }

    fun resetTimer(seconds: Long, manual: Boolean = true){
        if (manual){
            Log.d(TAG, "RESET at ${getCurrentFormattedTime()} - ${currentMode?.name} - $currentFullTime")
        }
        pauseTimer(callFromReset = true)
        _timeLeft.value = seconds
        resetFullTimeAndMode()
    }

    private fun resetFullTimeAndMode(){
        currentFullTime = null
        currentMode = null
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

    fun onStartPausePressed(timeLeft: Long, context: Context, mode: TimerMode){
        if(_isRunning.value) pauseTimer() else startTimer(initialSeconds = timeLeft, context=context, mode=mode)
    }

    fun onResetPressed(resetTime: Long){
        resetTimer(seconds = resetTime)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    companion object {
        private const val TAG = "TimerLog"
        fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", minutes, secs)
        }

        fun getCurrentFormattedTime(): String{
            return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        }
    }

}