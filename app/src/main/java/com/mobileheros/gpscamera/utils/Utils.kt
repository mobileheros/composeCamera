package com.mobileheros.gpscamera.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.net.Uri
import android.provider.Settings
//import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.Locale

object Utils {
    fun getVersionName(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }
    fun getVersionCode(context: Context): Int {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return 1
        }
    }
    fun goSetting(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
//    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
//        val planeProxy = image.planes[0]
//        val buffer: ByteBuffer = planeProxy.buffer
//        val bytes = ByteArray(buffer.remaining())
//        buffer.get(bytes)
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//    }

    fun formatTime(): String {
        return SimpleDateFormat(Global.dateFormat, Locale.getDefault()).format(System.currentTimeMillis())
    }
    fun getFormatLocationString(location: Location) : String {
        val build: StringBuilder = StringBuilder().apply {
            append(Location.convert(location.latitude, Location.FORMAT_DEGREES))
            append("°")
            append(if (location.latitude >= 0) "N" else "S")
            append(", ")
            append(Location.convert(location.longitude, Location.FORMAT_DEGREES))
            append("°")
            append(if (location.longitude >= 0) "E" else "W")
        }
        return build.toString()
    }
    fun formatVideoRecordTime(time: Long): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply { timeZone = TimeZone.GMT_ZONE }.format(time * 1000)
    }
}