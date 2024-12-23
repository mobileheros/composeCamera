package com.mobileheros.gpscamera.bean

import android.net.Uri

data class ImageBean(
    val id: Long,
    val uri: Uri,
    val name: String,
    val size: Int,
    val date: Long,
    val packageName: String?,
    val path: String?,
    var type: MediaType = MediaType.IMAGE,
    var duration: Int = 0
)

enum class MediaType {
    IMAGE,
    VIDEO
}