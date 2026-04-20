package com.s1g1.tomatoro.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.s1g1.tomatoro.MainActivity
import com.s1g1.tomatoro.R
import com.s1g1.tomatoro.TimerMode
import com.s1g1.tomatoro.database.sessions.Session
import com.s1g1.tomatoro.database.sessions.SessionRepository
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
        private const val TAG = "TimerServiceLogTag"
        const val TIMER_CHANNEL_ID = "TIMER_CHANNEL"
        const val TIMER_CHANNEL_NAME = "Timer Notifications"
        private var NOTIFICATION_ID = 1

        const val DURATION_EXTRA = "DURATION"
        const val MODE_EXTRA = "MODE"

        private val _secondsLeft = MutableStateFlow(0L)
        val secondsLeft = _secondsLeft.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()

        private val _currentFullSeconds = MutableStateFlow<Long?>(null)
        val currentFullSeconds = _currentFullSeconds.asStateFlow()

        private val _currentModeName = MutableStateFlow(TimerMode.getDefault().name)
    }

    private val repository: SessionRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null


    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TomatoroApp::TimerWakeLock")
        }
    }

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_CHANNEL_ID,
                TIMER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun startForegroundService(durationSeconds: Long){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildTimerNotification(contentText = "START...", isHighPriority = true))
        if (!wakeLock.isHeld) {
            wakeLock.acquire((durationSeconds + 60) * 1000L )
        }
        timerJob?.cancel()
        timerJob = serviceScope.launch(Dispatchers.Default) {
            try{
                if(_secondsLeft.value == 0L) _secondsLeft.value = durationSeconds
                val startTimeMillis = System.currentTimeMillis()
                val totalDurationMillis = _secondsLeft.value * 1000L
                while (_secondsLeft.value > 0){
                    delay(timeMillis = 500L)
                    val elapsedMillis = System.currentTimeMillis() - startTimeMillis
                    val remainingMillis = totalDurationMillis - elapsedMillis
                    if (remainingMillis <= 0) {
                        _secondsLeft.value = 0
                        break
                    }
                    val currentSeconds = remainingMillis / 1000
                    if (_secondsLeft.value != currentSeconds){
                        _secondsLeft.value = currentSeconds
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildTimerNotification(
                                contentText = formatTime(seconds = _secondsLeft.value)
                            )
                        )
                    }
                }
//                stopForeground(STOP_FOREGROUND_DETACH)
                stopForeground(STOP_FOREGROUND_REMOVE)
                val name: String = getString(TimerMode.fromName(_currentModeName.value).title)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildTimerNotification(
                        contentText = "(${(_currentFullSeconds.value?.div(60))?.toInt()}m): $name",
                        isHighPriority = true,
                        isFinalPopup = true
                    )
                )
                saveSessionToDatabase(
                    session = Session(
                        endTimestamp = System.currentTimeMillis(),
                        mode = TimerMode.fromName(name = _currentModeName.value),
                        duration = _currentFullSeconds.value ?: TimerMode.fromName(name = _currentModeName.value).defaultDuration.toLong()
                    )
                )
                NOTIFICATION_ID+=1
                _isRunning.value = false
                _secondsLeft.value = _currentFullSeconds.value ?: 0L
                _currentFullSeconds.value = null
                triggerVibration(this@TimerService)
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    private fun createActionIntent(action: TimerAction): PendingIntent{
        val intent = Intent(this, TimerService::class.java).apply{
            this.action = action.name
            putExtra(DURATION_EXTRA, _currentFullSeconds.value)
        }

        return PendingIntent.getService(
            this,
            action.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun buildTimerNotification(
        contentText: String,
        isHighPriority: Boolean = false,
        isFinalPopup: Boolean = false,
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer) // ICON
            .setContentTitle(getString(R.string.app_name)) // TITLE (app name)
            .setContentText(contentText) // TEXT (mm:ss)
            .setOngoing(true)
            .setSilent(!isHighPriority) // VIBRATION+SOUND only for start and finish
            .setOnlyAlertOnce(isHighPriority)
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
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
            .apply{
                if(isFinalPopup){
                    setFullScreenIntent(pendingIntent, true)
                } else {
                    addAction(
                        R.drawable.ic_reset,
                        TimerAction.RESET.name,
                        createActionIntent(TimerAction.RESET)
                    )

                    if (_isRunning.value){
                        addAction(
                            R.drawable.ic_pause,
                            TimerAction.PAUSE.name,
                            createActionIntent(TimerAction.PAUSE)
                        )
                    } else {
                        addAction(
                            R.drawable.ic_start,
                            TimerAction.START.name,
                            createActionIntent(TimerAction.START)
                        )
                    }
                }
            }
            .build()
    }

    private fun saveSessionToDatabase(session: Session){
        serviceScope.launch(Dispatchers.IO) {
            try{
                repository.saveSession(session = session)
                Log.d(TAG, "FINISH - ${getCurrentFormattedTime()} - ${session.duration} - ${getString(TimerMode.fromName(_currentModeName.value).title)}")
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

                notificationManager.notify(
                    NOTIFICATION_ID,
                    buildTimerNotification(
                        contentText = formatTime(seconds = _secondsLeft.value)
                    )
                )
            }
            TimerAction.RESET.name -> {
                Log.d(TAG, "$actionName - ${getCurrentFormattedTime()} - $durationSeconds")
                _isRunning.value = false

                timerJob?.cancel()

                _secondsLeft.value = durationSeconds
                _currentModeName.value = modeName
                _currentFullSeconds.value = null

                if (wakeLock.isHeld) wakeLock.release()
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
        notificationManager.cancel(NOTIFICATION_ID)
        if (wakeLock.isHeld) wakeLock.release()
        stopSelf()
        super.onDestroy()
    }
}