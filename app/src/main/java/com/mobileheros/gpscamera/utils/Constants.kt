package com.mobileheros.gpscamera.utils

import com.mobileheros.gpscamera.bean.ResolutionBean

object Constants {
    const val LOCAL_CONFIG = "local_config"
    const val PRODUCT_ID = "autosub"

    const val AD_OPEN_APP = "openAppAd"

    const val SWITCH_COMPASS = "switch_compass"
    const val SWITCH_MAP = "switch_map"
    const val SWITCH_GPS = "switch_gps"
    const val SWITCH_ADDRESS = "switch_address"
    const val SWITCH_ALTITUDE = "switch_altitude"
    const val SWITCH_WEATHER = "switch_weather"
    const val SWITCH_LOGO = "switch_logo"
    const val SWITCH_TEXT = "switch_text"
    const val SWITCH_TAG = "switch_tag"
    const val TEXT_CONTENT = "text_content"
    const val TAG_LIST = "tag_list"
    const val DATE_FORMAT = "date_format"
    const val SCALE = "scale"
    const val PHOTO_PREFIX = "photo_prefix"
    const val PHOTO_PREFIX_DEFAULT = "TimeCamera_I"
    const val VIDEO_PREFIX = "video_prefix"
    const val VIDEO_PREFIX_DEFAULT = "TimeCamera_V"
    const val CUSTOM_DIR = "custom_dir"
    const val CUSTOM_DIR_DEFAULT = "gpsCamera"
    const val IS_RATED = "isRated"
    const val FIRST_PHOTO = "first_photo"
    const val RATED_SHOW_TIME = "show_time"
    const val PRIVACY_URL = "https://d1g5ijv7r006oi.cloudfront.net/watermark_pro_privacy.html"

    val FORMAT_LIST = listOf(
        "HH:mm:ss dd/MM/yyyy",
        "dd-MM-yyyy HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "hh:mm:ss MMM d,yyyy",
        "MMM d,yyyy hh:mm:ss"
    )

    const val MULTI = "×"
    val image_resolution_list = listOf(
        ResolutionBean(false, 480, 640),
        ResolutionBean(false, 1200, 1600),
        ResolutionBean(true, 1920, 2560),
        ResolutionBean(true, 2160, 3840),
        ResolutionBean(true, 3000, 4000),
        ResolutionBean(true, 2988, 5312),
        ResolutionBean(true, 4320, 7680),
        ResolutionBean(true, 6000, 8000),
    )
    val video_resolution_list = listOf(
        ResolutionBean(false, 480, 640).apply { title = "480P" },
        ResolutionBean(true, 720, 1280).apply { title = "720P" },
        ResolutionBean(true, 1080, 1920).apply { title = "1080P" },
    )

}