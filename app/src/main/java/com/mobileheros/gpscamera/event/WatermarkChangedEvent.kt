package com.mobileheros.gpscamera.event

class WatermarkChangedEvent(var type: Int, var text: String = "") {
    companion object{
        const val TYPE_GPS = 0
        const val TYPE_MAP = 1
        const val TYPE_ALTITUDE = 2
        const val TYPE_ADDRESS = 3
        const val TYPE_WEATHER = 4
        const val TYPE_FORMAT = 5
        const val TYPE_TEXT = 6
        const val TYPE_TAG = 7
        const val TYPE_CHANGED = 8
    }
}