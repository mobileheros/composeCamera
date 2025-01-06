package com.mobileheros.gpscamera

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.mobileheros.gpscamera.databinding.ActivityMainBinding
import com.mobileheros.gpscamera.dialog.RateDialog
import com.mobileheros.gpscamera.ui.theme.GpsCameraTheme
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import com.mobileheros.gpscamera.utils.getData
import com.mobileheros.gpscamera.utils.localConfig
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getLocalConfig()
//        enableEdgeToEdge()
//        setContent {
//            GpsCameraTheme {
//                AppNavGraph()
//            }
//        }

        val boolean = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        Logger.d("available: $boolean")
        if (ContextCompat.checkSelfPermission(this, Permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).getCurrentLocation(100,CancellationTokenSource().token).addOnSuccessListener {
                Logger.d(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        PlayBillingHelper(application).queryPurchases(this)
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
                if (Global.isVip.value) this.getData(Constants.SWITCH_LOGO, false) else false
            Global.scale = this.getData(Constants.SCALE, 0.5f)
        }
    }
}