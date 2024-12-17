package com.mobileheros.gpscamera

import android.app.Application
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class CameraApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PlayBillingHelper.getInstance(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}