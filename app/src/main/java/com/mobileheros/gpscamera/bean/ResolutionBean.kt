package com.mobileheros.gpscamera.bean

import com.mobileheros.gpscamera.utils.Constants

data class ResolutionBean(var isPro: Boolean, var width: Int, var height: Int, var frame: Int = 0, var isChecked:Boolean = false,) {
    var title: String = ""
        get() {
            return if (frame > 0) {
                frame.toString() + "FPS"
            } else if (field.isNotEmpty()) {
                field
            } else {
                "$height${Constants.MULTI}$width"
            }
        }
}