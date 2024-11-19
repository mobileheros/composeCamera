package com.mobileheros.gpscamera.utils

import android.net.Uri

object Global {
    var scale: Float = 0.5f
    var text: String = ""
    var logo: Boolean = false
    var tag: Boolean = true
    var textSwitch: Boolean = false
    var weather: Boolean = false
    var map: Boolean = true
    var address: Boolean = true
    var altitude: Boolean = true
    var gps: Boolean = true
    var compass: Boolean = true
    var isVip: Boolean = false
    var isVideo: Boolean = false
    var hasShowRateDialog = false
    var firstPhoto = false

    var showOpenAd: Boolean = false



    var dateFormat: String = Constants.FORMAT_LIST[0]
    var timeFormat: String = "HH:mm:ss"
    var imageUri: Uri? = null
}