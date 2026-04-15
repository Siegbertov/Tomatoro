package com.s1g1.tomatoro.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.s1g1.tomatoro.MainActivity
import com.s1g1.tomatoro.R
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.Session
import com.s1g1.tomatoro.database.SessionRepository
import com.s1g1.tomatoro.triggerVibration
import com.s1g1.tomatoro.ui.timer.TimerAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.koin.core.component.inject
import java.util.Locale


class TimerService : Service(), KoinComponent {
    companion object {
        fun getCurrentFormattedTime(): String{
            return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        }
        fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val secs = seconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
        private const val TAG = "TimerService"
        private const val NOTIFICATION_ID = 1
        const val TIMER_CHANNEL_ID = "TIMER_CHANNEL"
        const val TIMER_CHANNEL_NAME = "Timer Notifications"

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

    private val repository: SessionRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getString(R.string.app_name))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                TIMER_CHANNEL_NAME,
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
        startForeground(NOTIFICATION_ID, notificationBuilder.setContentText("START...").build())

        //MY PERSONAL TIMER (cancel previous)
        timerJob?.cancel()

        //MY PERSONAL TIMER (start new)
        timerJob = serviceScope.launch {
            // SETTING UP
            if(_secondsLeft.value == 0L){
                _secondsLeft.value = durationSeconds
            }
            // TICKING
            while (_secondsLeft.value > 0){
                delay(timeMillis = 1000L)
                _secondsLeft.value -= 1
                updateNotification( newMessage = formatTime(seconds = _secondsLeft.value) )
            }
            // FINISHED
            val name: String = getString(TimerMode.fromName(_currentModeName.value).title)
            saveSessionToDatabase(
                session = Session(
                    endTimestamp = System.currentTimeMillis(),
                    mode = TimerMode.fromName(name = _currentModeName.value),
                    duration = _currentFullSeconds.value ?: TimerMode.fromName(name = _currentModeName.value).defaultDuration.toLong()
                )
            )
            // UPDATE LAST NOTIFICATION
            updateNotification( newMessage = "DONE: $name", isFinal = true )

            _isRunning.value = false
            _secondsLeft.value = _currentFullSeconds.value ?: 0L
            _currentFullSeconds.value = null

            triggerVibration(this@TimerService)
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun updateNotification(
        newMessage: String,
        isFinal: Boolean = false
    ){
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = notificationBuilder
            .setContentText(newMessage)
            .setOnlyAlertOnce(!isFinal)
            .apply{
                if (isFinal){
                    setCategory(NotificationCompat.CATEGORY_ALARM)
                    setPriority(NotificationCompat.PRIORITY_MAX)
                    setDefaults(NotificationCompat.DEFAULT_ALL)
                    setFullScreenIntent(pendingIntent, true)
                }
            }
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun saveSessionToDatabase(session: Session){
        serviceScope.launch(Dispatchers.IO) {
            try{
                repository.saveSession(session = session)
                Log.d(TAG, "FINISH - ${getCurrentFormattedTime()} - ${_currentFullSeconds.value} - ${getString(TimerMode.fromName(_currentModeName.value).title)}")
            } catch (e: Exception){
                Log.d(TAG, "Failed to save session", e)
            }
        }
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
                _isRunning.value = false
                timerJob?.cancel()
            }
            TimerAction.RESET.name -> {
                Log.d(TAG, "$actionName - ${getCurrentFormattedTime()} - $durationSeconds")
                _isRunning.value = false

                timerJob?.cancel()

                _secondsLeft.value = durationSeconds
                _currentModeName.value = modeName
                _currentFullSeconds.value = null
                stopForeground(STOP_FOREGROUND_REMOVE)

                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        timerJob?.cancel()
        super.onDestroy()
    }
}