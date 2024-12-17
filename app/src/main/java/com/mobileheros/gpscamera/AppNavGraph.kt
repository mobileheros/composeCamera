package com.mobileheros.gpscamera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobileheros.gpscamera.ui.camera.CameraScreen
import com.mobileheros.gpscamera.ui.privacy.PrivacyScreen
import com.mobileheros.gpscamera.ui.setting.SettingScreen
import com.mobileheros.gpscamera.ui.subscribe.SubscribeScreen
import com.mobileheros.gpscamera.ui.watermark.WatermarkSettingScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    startDestination: String = AppScreens.CAMERA_SCREEN,
    navActions: AppNavigationAction = remember(navController) {
        AppNavigationAction(navController)
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            AppScreens.CAMERA_SCREEN,
        ) {
            CameraScreen(navWatermarkSetting = { navActions.navigationToWatermark() },
                navSubscribe = { navActions.navigationToSubscribe() })
        }
        composable(
            AppScreens.WATERMARK_SCREEN,
        ) {
            WatermarkSettingScreen(navBack = {
                navController.navigateUp()
            }, navSetting = {
                navActions.navigationToSetting()
            }, navSubscribe = { navActions.navigationToSubscribe() })
        }
        composable(
            AppScreens.SETTING_SCREEN,
        ) {
            SettingScreen(
                navBack = { navController.navigateUp() },
                navPrivacy = { navActions.navigationToPrivacy() })
        }
        composable(
            AppScreens.SUBSCRIBE_SCREEN,
        ) {
            SubscribeScreen(navBack = { navController.navigateUp() },)
        }
        composable(
            AppScreens.PRIVACY_SCREEN,
        ) {
            PrivacyScreen(navBack = { navController.navigateUp() })
        }
    }
}