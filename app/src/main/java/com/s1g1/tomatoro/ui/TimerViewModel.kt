package com.s1g1.tomatoro.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimerViewModel : ViewModel(){

    private val _timeLeft = MutableStateFlow(60L * 25)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    var currentFullTime: Long? = null
    var currentModeString: String? = null

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(initialSeconds: Long, modeString: String){
        if (_isRunning.value) return

        if (currentFullTime==null) {
            currentFullTime = initialSeconds
            currentModeString = modeString
        }
        println("STARTED at ${getCurrentFormattedTime()} - $currentModeString - $currentFullTime")

        _isRunning.value = true
        countDownTimer = object : CountDownTimer(initialSeconds*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                println("CONGRATULATION: passed $currentModeString with $currentFullTime at ${getCurrentFormattedTime()}")
                _timeLeft.value = 0
                _isRunning.value = false
                resetFullTimeAndMode()
            }
        }.start()
    }

    fun pauseTimer(callFromReset: Boolean = false){
        if (!callFromReset){
            println("PAUSED at ${getCurrentFormattedTime()} - $currentModeString - $currentFullTime")
        }
        _isRunning.value = false
        countDownTimer?.cancel()
    }

    fun resetTimer(seconds: Long, manual: Boolean = true){
        if (manual){
            println("RESET at ${getCurrentFormattedTime()} - $currentModeString - $currentFullTime")
        }
        pauseTimer(callFromReset = true)
        _timeLeft.value = seconds
        resetFullTimeAndMode()
    }

    private fun resetFullTimeAndMode(){
        currentFullTime = null
        currentModeString = null
    }

    fun onStartPausePressed(timeLeft: Long, modeString: String){
        if(_isRunning.value) pauseTimer() else startTimer(initialSeconds = timeLeft, modeString=modeString)
    }

    fun onResetPressed(resetTime: Long){
        resetTimer(seconds = resetTime)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    companion object {
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