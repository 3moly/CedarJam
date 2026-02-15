package com.moly3.cedarjam.core.ui.service

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import com.moly3.cedarjam.core.domain.service.AppContextProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


actual object KVibrator : KoinComponent {

    private val appContextProvider: AppContextProvider by inject()

    private fun getVibrator(): Vibrator {
        val context = appContextProvider.getApplicationContext() as Context
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    actual fun vibrateShort() {
        val vibrator = getVibrator()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    50,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(50)
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    actual fun vibrateLong() {
        val vibrator = getVibrator()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(300)
        }
    }
}