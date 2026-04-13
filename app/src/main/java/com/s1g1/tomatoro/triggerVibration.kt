package com.s1g1.tomatoro

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

@SuppressLint("ServiceCast")
fun triggerVibration(context: Context){
    val vibrator = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()){
        Log.d("VIBRO", "FOUND VIBRATOR, Trying to vibrate...")

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 250, 100, 250), -1),
                audioAttributes
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}