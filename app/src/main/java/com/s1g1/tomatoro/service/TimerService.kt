package com.s1g1.tomatoro.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.s1g1.tomatoro.R
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.triggerVibration
import com.s1g1.tomatoro.ui.timer.TimerAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class TimerService : Service(){
    companion object {
        fun getCurrentFormattedTime(): String{
            return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        }
        fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
        private const val TAG = "TimerLog"
        private const val NOTIFICATION_ID = 1
        const val TIMER_CHANNEL_ID = "TIMER_CHANNEL"
        const val DURATION_EXTRA = "DURATION"
        const val MODE_EXTRA = "MODE"

        private val _secondsLeft = MutableStateFlow(0L)
        val secondsLeft = _secondsLeft.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()

        private val _currentFullSeconds = MutableStateFlow<Long?>(null)
        val currentFullSeconds = _currentFullSeconds.asStateFlow()

        private val _currentModeName = MutableStateFlow(TimerMode.getDefault().name)
        val currentModeName = _currentModeName.asStateFlow()
    }

    private var timer: CountDownTimer? = null

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getString(R.string.app_name))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Timer Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    private fun startForegroundService(durationSeconds: Long){
        // CREATES NOTIFICATION CHANNEL (for API 26+)
        createNotificationChannel()
        // START FOREGROUND
        startForeground(1, notificationBuilder.setContentText("START...").build())
        // START TIMER
        timer?.cancel()
        timer = object : CountDownTimer(durationSeconds*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _secondsLeft.value = millisUntilFinished / 1000
                updateNotification(newMessage = formatTime(seconds = _secondsLeft.value))
            }
            override fun onFinish() {
                val name: String = getString(TimerMode.fromName(_currentModeName.value).title)
                Log.d(TAG, "FINISH - ${getCurrentFormattedTime()} - ${_currentFullSeconds.value} - $name")
                updateNotification(
                    newMessage = "DONE: $name"
                )
                _isRunning.value = false
                _secondsLeft.value = _currentFullSeconds.value ?: 0L
                _currentFullSeconds.value = null
                triggerVibration(this@TimerService)
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }.start()
    }

    private fun updateNotification(newMessage: String){
        val notification = notificationBuilder.setContentText(newMessage).build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val actionName = intent?.action
        val durationSeconds = intent?.getLongExtra(DURATION_EXTRA, 0L) ?: 0L
        val modeName = intent?.getStringExtra(MODE_EXTRA) ?: TimerMode.getDefault().name

        when(actionName){
            TimerAction.START.name -> {
                Log.d(TAG, "$actionName - ${getCurrentFormattedTime()} - $durationSeconds")

                if (_currentFullSeconds.value == null) {
                    _currentFullSeconds.value = durationSeconds
                }

                _isRunning.value = true
                startForegroundService(durationSeconds=durationSeconds)
            }
            TimerAction.PAUSE.name -> {
                Log.d(TAG, "$actionName - ${getCurrentFormattedTime()}")
                updateNotification(newMessage = "PAUSED: ${formatTime(seconds = _secondsLeft.value)}")
                _isRunning.value = false
                timer?.cancel()
            }
            TimerAction.RESET.name -> {
                Log.d(TAG, "$actionName - ${getCurrentFormattedTime()} - $durationSeconds")
                _isRunning.value = false

                timer?.cancel()
                _secondsLeft.value = durationSeconds
                _currentModeName.value = modeName

                _currentFullSeconds.value = null

                stopForeground(STOP_FOREGROUND_REMOVE)
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}