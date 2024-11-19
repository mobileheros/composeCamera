package com.mobileheros.gpscamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mobileheros.gpscamera.ui.theme.GpsCameraTheme
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import com.mobileheros.gpscamera.utils.getData
import com.mobileheros.gpscamera.utils.localConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getLocalConfig()
        setContent {
            GpsCameraTheme {
                AppNavGraph()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        PlayBillingHelper(applicationContext).queryPurchases(this)
    }

    private fun getLocalConfig() {
        with(localConfig) {
            Global.compass = this.getData(Constants.SWITCH_COMPASS, false)
            Global.gps = this.getData(Constants.SWITCH_GPS, true)
            Global.altitude = this.getData(Constants.SWITCH_ALTITUDE, true)
            Global.address = this.getData(Constants.SWITCH_ADDRESS, true)
            Global.map = this.getData(Constants.SWITCH_MAP, true)
            Global.weather = this.getData(Constants.SWITCH_WEATHER, false)
            Global.textSwitch = this.getData(Constants.SWITCH_TEXT, false)
            Global.tag = this.getData(Constants.SWITCH_TAG, false)
            Global.text = this.getData(Constants.TEXT_CONTENT, "")
            Global.dateFormat = this.getData(Constants.DATE_FORMAT, Constants.FORMAT_LIST[0])
            Global.logo =
                if (Global.isVip) this.getData(Constants.SWITCH_LOGO, false) else false
            Global.scale = this.getData(Constants.SCALE, 0.5f)
        }
    }
}