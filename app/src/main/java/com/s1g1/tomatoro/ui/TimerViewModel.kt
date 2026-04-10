package com.s1g1.tomatoro.ui

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel : ViewModel(){

    private val _timeLeft = MutableStateFlow(60L * 25)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    fun startTimer(initialSeconds: Long){
        if (_isRunning.value) return

        _isRunning.value = true
        countDownTimer = object : CountDownTimer(initialSeconds*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isRunning.value = false
            }
        }.start()
    }

    fun pauseTimer(){
        _isRunning.value = false
        countDownTimer?.cancel()
    }

    fun resetTimer(seconds: Long){
        pauseTimer()
        _timeLeft.value = seconds
    }

    fun onStartPausePressed(timeLeft: Long){
        if(_isRunning.value) pauseTimer() else startTimer(initialSeconds = timeLeft)
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
    }

}