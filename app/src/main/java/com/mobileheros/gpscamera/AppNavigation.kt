package com.mobileheros.gpscamera

import androidx.navigation.NavHostController
import com.mobileheros.gpscamera.AppScreens.PRIVACY_SCREEN
import com.mobileheros.gpscamera.AppScreens.SETTING_SCREEN
import com.mobileheros.gpscamera.AppScreens.SUBSCRIBE_SCREEN
import com.mobileheros.gpscamera.AppScreens.WATERMARK_SCREEN

object AppScreens{
    const val CAMERA_SCREEN = "camera"
    const val WATERMARK_SCREEN = "watermark"
    const val SETTING_SCREEN = "setting"
    const val SUBSCRIBE_SCREEN = "subscribe"
    const val PRIVACY_SCREEN = "privacy"
}
class AppNavigationAction(private val navController: NavHostController){
    fun navigationToWatermark() {
        navController.navigate(WATERMARK_SCREEN)
    }
    fun navigationToSetting() {
        navController.navigate(SETTING_SCREEN)
    }
    fun navigationToSubscribe() {
        navController.navigate(SUBSCRIBE_SCREEN)
    }
    fun navigationToPrivacy() {
        navController.navigate(PRIVACY_SCREEN)
    }
}