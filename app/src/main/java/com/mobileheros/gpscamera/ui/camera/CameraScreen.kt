//@file:OptIn(
//    ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class,
//    ExperimentalComposeUiApi::class
//)
//
//package com.mobileheros.gpscamera.ui.camera
//
//import android.content.ContentValues
//import android.content.Context
//import android.content.Context.SENSOR_SERVICE
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.Matrix
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.location.Geocoder
//import android.net.Uri
//import android.os.Build
//import android.provider.MediaStore
//import android.view.OrientationEventListener
//import android.view.Surface
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.CameraSelector.LensFacing
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
//import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
//import androidx.camera.core.ImageCapture.FLASH_MODE_ON
//import androidx.camera.core.ImageProxy
//import androidx.camera.core.resolutionselector.AspectRatioStrategy
//import androidx.camera.core.resolutionselector.ResolutionSelector
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.MediaStoreOutputOptions
//import androidx.camera.video.Quality
//import androidx.camera.video.QualitySelector
//import androidx.camera.video.Recorder
//import androidx.camera.video.Recording
//import androidx.camera.video.VideoCapture
//import androidx.camera.video.VideoRecordEvent
//import androidx.camera.view.PreviewView
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.keyframes
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.ui.graphics.asAndroidBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.core.content.ContextCompat
//import androidx.core.graphics.scale
//import androidx.core.util.Consumer
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.LifecycleOwner
//import coil.compose.AsyncImage
//import com.drake.net.Get
//import com.drake.net.time.Interval
//import com.drake.net.utils.scopeNet
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberMultiplePermissionsState
//import com.google.accompanist.permissions.rememberPermissionState
//import com.google.accompanist.permissions.shouldShowRationale
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.mobileheros.gpscamera.R
//import com.mobileheros.gpscamera.utils.Constants
//import com.mobileheros.gpscamera.utils.Global
//import com.mobileheros.gpscamera.utils.ImageSaveUtils.saveToAlbum
//import com.mobileheros.gpscamera.utils.Utils
//import com.mobileheros.gpscamera.utils.getData
//import com.mobileheros.gpscamera.utils.localConfig
//import com.orhanobut.logger.Logger
//import com.watermark.androidwm_light.WatermarkBuilder
//import com.watermark.androidwm_light.bean.WatermarkImage
//import com.watermark.androidwm_light.bean.WatermarkPosition
//import dev.shreyaspatil.capturable.capturable
//import dev.shreyaspatil.capturable.controller.CaptureController
//import dev.shreyaspatil.capturable.controller.rememberCaptureController
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.Timer
//import java.util.TimerTask
//import java.util.concurrent.TimeUnit
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//
//@Composable
//fun CameraScreen(navWatermarkSetting: () -> Unit, navSubscribe: () -> Unit) {
//    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    val degrees = remember { mutableFloatStateOf(0f) }
//    val addressStr = remember { mutableStateOf("") }
//    val weatherStr = remember { mutableStateOf("") }
//    var locationStr by remember { mutableStateOf("") }
//    var altitudeStr by remember { mutableStateOf("") }
//    Scaffold { innerPadding ->
//        Column(modifier = Modifier.padding(innerPadding)) {
//            context = LocalContext.current
//            CameraContent(
//                degrees.floatValue,
//                locationStr,
//                altitudeStr,
//                addressStr.value,
//                weatherStr.value,
//                navWatermarkSetting,
//                navSubscribe
//            )
//        }
//    }
//    DeviceOrientationListener(LocalContext.current, degrees)
//    val locationPermissionState = rememberMultiplePermissionsState(
//        listOf(
//            android.Manifest.permission.ACCESS_COARSE_LOCATION,
//            android.Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    )
//    val autoRequest = remember {
//        mutableStateOf(true)
//    }
//    var state = locationPermissionState.permissions.find { it.status.isGranted }
//    if (state == null) {
//        if (autoRequest.value) {
//            LaunchedEffect(locationPermissionState) {
//                locationPermissionState.launchMultiplePermissionRequest()
//            }
//            autoRequest.value = false
//        }
//    } else {
//        fusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(LocalContext.current)
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            location?.let {
//                Logger.e(location.toString())
//                locationStr = Utils.getFormatLocationString(it)
//                altitudeStr = location.altitude.toString()
//                getAddressInfo(context, location.latitude, location.longitude, addressStr)
//                getWeather(location.latitude, location.longitude, weatherStr)
//            }
//        }
//    }
//}
//
//@Composable
//fun CameraContent(
//    degree: Float,
//    locationStr: String,
//    altitudeStr: String,
//    addressStr: String,
//    weatherStr: String,
//    navWatermarkSetting: () -> Unit,
//    navSubscribe: () -> Unit,
//) {
//    val cameraPermissionState = rememberPermissionState(
//        android.Manifest.permission.CAMERA
//    )
//    val autoRequest = remember {
//        mutableStateOf(true)
//    }
//    cameraPermissionState.status
//
//    if (cameraPermissionState.status.isGranted) {
//
//        Column {
//            TopBar(navWatermarkSetting, navSubscribe)
//            CameraArea(
//                degree = degree,
//                locationStr = locationStr,
//                altitudeStr = altitudeStr,
//                addressStr = addressStr,
//                weatherStr = weatherStr
//            )
//            if (ratio.floatValue == 3 / 4f) {
//                Spacer(modifier = Modifier.weight(1f))
//                CameraFunctionRow(modifier = Modifier) {
//                    Logger.e("onFaceChanged")
//                    lensFacing.value = (lensFacing.value + 1) % 2
//                }
//            }
//            ModeRow()
//        }
//    } else {
//        if (autoRequest.value) {
//            LaunchedEffect(cameraPermissionState) {
//                cameraPermissionState.launchPermissionRequest()
//            }
//            autoRequest.value = false
//        }
//        Column(
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
//                // If the user has denied the permission but the rationale can be shown,
//                // then gently explain why the app requires this permission
//                "The camera is important for this app. Please grant the permission."
//            } else {
//                // If it's the first time the user lands on this feature, or the user
//                // doesn't want to be asked again for this permission, explain that the
//                // permission is required
//                "Camera permission required for this feature to be available. " +
//                        "Please grant the permission"
//            }
//            val context = LocalContext.current
//            Text(
//                text = textToShow,
//                modifier = Modifier.padding(horizontal = 20.dp),
//                style = TextStyle(
//                    color = colorResource(id = R.color.text_222222),
//                    textAlign = TextAlign.Center
//                )
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//            Button(
//                onClick = {
//                    if (cameraPermissionState.status.shouldShowRationale) {
//                        cameraPermissionState.launchPermissionRequest()
//                    } else {
//                        Utils.goSetting(context)
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.yellow_main))
//            ) {
//                Text("Request permission")
//            }
//        }
//    }
//
//}
//
//@Composable
//fun TopBar(navWatermarkSetting: () -> Unit, navSubscribe: () -> Unit) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(50.dp)
//            .background(color = Color.White)
//    ) {
//        FlashPanel()
//        Spacer(modifier = Modifier.weight(1f))
//        if (isVideo.value) {
//            if (!recordFinished.value) {
//                Text(
//                    recordTime.value,
//                    color = colorResource(id = R.color.text_222222),
//                    fontSize = 16.sp
//                )
//            } else {
//                VideoSpinner(navSubscribe)
//            }
//        } else {
//            PhotoSpinner(navSubscribe)
//        }
//        Spacer(modifier = Modifier.weight(1f))
//        Image(
//            painter = painterResource(id = R.mipmap.ic_tag),
//            modifier = Modifier
//                .clickable { navWatermarkSetting() }
//                .padding(12.dp),
//            colorFilter = ColorFilter.tint(Color.Black), contentDescription = null
//        )
//    }
//}
//
//var targetResolution = mutableStateOf(android.util.Size(1600, 1200))
//var videoTargetResolution = mutableStateOf(android.util.Size(640, 480))
//var ratio = mutableFloatStateOf(3 / 4f)
//var flashMode = mutableIntStateOf(FLASH_MODE_AUTO)
//var lensFacing = mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
//var videoQuality = mutableStateOf(Quality.SD)
//
//@Composable
//fun CameraArea(
//    degree: Float,
//    locationStr: String,
//    altitudeStr: String,
//    addressStr: String,
//    weatherStr: String
//) {
//    ConstraintLayout(
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(ratio.value)
//            .background(Color.White)
//    ) {
//        val (camera, leftBottom, rightBottom, leftTop, rightTop, bottomFunction) = createRefs()
//        CameraPreviewScreen(modifier = Modifier.constrainAs(camera) {
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        })
//        TextWatermark(
//            modifier = Modifier.constrainAs(leftBottom) {
//                bottom.linkTo(camera.bottom, margin = 10.dp)
//                start.linkTo(camera.start, margin = 10.dp)
//            },
//            degrees = degree,
//            locationStr = locationStr,
//            altitudeStr = altitudeStr,
//            addressStr = addressStr,
//            weatherStr = weatherStr
//        )
//        if (Global.compass) {
//            CompassWatermark(modifier = Modifier.constrainAs(leftTop) {
//                top.linkTo(camera.top, margin = 10.dp)
//                start.linkTo(camera.start, margin = 10.dp)
//            }, degree = degree)
//        }
//        LogoWatermark(modifier = Modifier.constrainAs(rightTop) {
//            top.linkTo(camera.top, margin = 10.dp)
//            end.linkTo(camera.end, margin = 10.dp)
//        }, degree = degree)
//        if (ratio.value == 9 / 16f) {
//            CameraFunctionRow(modifier = Modifier.constrainAs(bottomFunction) {
//                bottom.linkTo(camera.bottom)
//                start.linkTo(camera.start)
//            }, onFaceChanged = {
//                Logger.e("onFaceChanged")
//                lensFacing.value = (lensFacing.value + 1) % 2
//            })
//        } else {
//
//        }
//    }
//
//}
//
//var textWatermarkCaptureController: CaptureController? = null
//var compassWatermarkCaptureController: CaptureController? = null
//var logoWatermarkCaptureController: CaptureController? = null
//
//var leftBottomSize: IntSize = IntSize(0, 0)
//var leftTopSize: IntSize = IntSize(0, 0)
//var rightTopSize: IntSize = IntSize(0, 0)
//
//fun textOffsetY(degree: Float): Dp {
//    Logger.e("$degree--$cameraSize---$leftBottomSize")
//    return when (degree) {
//        0f -> 0.dp
//        90f -> (((leftBottomSize.width - leftBottomSize.height) / 2) / density).dp
//        180f -> ((cameraSize.height - leftBottomSize.height) / density).dp - 20.dp
//        270f -> ((cameraSize.width - (leftBottomSize.width + leftBottomSize.height) / 2) / density).dp - 20.dp
//        else -> 0.dp
//    }
//}
//
//fun textOffsetX(degree: Float): Dp {
//    return when (degree) {
//        0f -> 0.dp
//        90f -> (-(cameraSize.height * 2 - leftBottomSize.width - leftBottomSize.height) / density / 2).dp + 20.dp
//        180f -> (-(cameraSize.width - leftBottomSize.width) / density).dp + 20.dp
//        270f -> ((-leftBottomSize.height + leftBottomSize.width) / density / 2).dp
//        else -> 0.dp
//    }
//}
//
//fun compassOffsetY(degree: Float): Dp {
//    Logger.e("$degree--$cameraSize---$leftTopSize")
//    return when (degree) {
//        0f -> 0.dp
//        90f -> (-(cameraSize.width - leftTopSize.width) / density).dp + 20.dp
//        180f -> (-(cameraSize.height - leftTopSize.height) / density).dp + 20.dp
//        270f -> 0.dp
//        else -> 0.dp
//    }
//}
//
//fun compassOffsetX(degree: Float): Dp {
//    return when (degree) {
//        0f -> 0.dp
//        90f -> 0.dp
//        180f -> (-(cameraSize.width - leftTopSize.width) / density).dp + 20.dp
//        270f -> (-(cameraSize.height - leftTopSize.height) / density).dp + 20.dp
//        else -> 0.dp
//    }
//}
//
//fun logoOffsetY(degree: Float): Dp {
//    Logger.e("$degree--$cameraSize---$leftTopSize")
//    return when (degree) {
//        0f -> 0.dp
//        90f -> (((rightTopSize.width - rightTopSize.height) / 2) / density).dp
//        180f -> (-(cameraSize.height - rightTopSize.height) / density).dp + 20.dp
//        270f -> (-(cameraSize.width - (rightTopSize.width + rightTopSize.height) / 2) / density).dp + 20.dp
//        else -> 0.dp
//    }
//}
//
//fun logoOffsetX(degree: Float): Dp {
//    return when (degree) {
//        0f -> 0.dp
//        90f -> ((cameraSize.height * 2 - rightTopSize.width - rightTopSize.height) / density / 2).dp - 20.dp
//        180f -> ((cameraSize.width - rightTopSize.width) / density).dp - 20.dp
//        270f -> ((-rightTopSize.height + rightTopSize.width) / density / 2).dp
//        else -> 0.dp
//    }
//}
//
//@Composable
//fun TextWatermark(
//    modifier: Modifier,
//    degrees: Float,
//    locationStr: String,
//    altitudeStr: String,
//    addressStr: String,
//    weatherStr: String
//) {
//    textWatermarkCaptureController = rememberCaptureController()
//    Column(
//        modifier.then(
//            Modifier
//                .onGloballyPositioned { layoutCoordinates ->
//                    leftBottomSize = layoutCoordinates.size
//                }
//                .rotate(degrees)
//                .offset(x = (textX.value), y = (textY.value))
//                .capturable(textWatermarkCaptureController!!)
//                .background(Color.Gray.copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
//                .fillMaxWidth(0.5f)
//                .padding(8.dp)
//        )
//
//    ) {
//        if (Global.dateFormat != stringResource(R.string.display_off)) {
//            var time by remember { mutableStateOf(Utils.formatTime()) }
//            var timer = Timer()
//            ComposableLifecycle { _, event ->
//                when (event) {
//                    Lifecycle.Event.ON_RESUME -> {
//                        timer.cancel()
//                        timer = Timer()
//                        timer.schedule(object : TimerTask() {
//                            override fun run() {
//                                time = Utils.formatTime()
//                            }
//                        }, 0, 1000)
//                    }
//
//                    Lifecycle.Event.ON_PAUSE -> {
//                        timer.cancel()
//                    }
//
//                    else -> {}
//                }
//            }
//            Text(
//                text = time,
//                style = TextStyle(
//                    color = Color.White,
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            )
//        }
//        Row(modifier = Modifier.wrapContentHeight()) {
////            VerticalDivider(
////                modifier = Modifier
////                    .padding(start = 2.dp, end = 5.dp)
////                    .wrapContentHeight(),
////                thickness = 2.dp,
////                color = colorResource(id = R.color.yellow_main)
////            )
//            val style = TextStyle(color = Color.White, fontSize = 10.sp)
//            Column {
//                if (Global.weather && weatherStr.isNotEmpty()) {
//                    Text(
//                        text = weatherStr,
//                        style = style,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//                if (Global.address && addressStr.isNotEmpty()) {
//                    Text(
//                        text = addressStr,
//                        style = style,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//                if (Global.gps && locationStr.isNotEmpty()) {
//                    Text(
//                        text = locationStr,
//                        style = style,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//                if (Global.altitude && altitudeStr.isNotEmpty()) {
//                    Text(
//                        text = altitudeStr,
//                        style = style,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//                if (Global.textSwitch && Global.text.isNotEmpty()) {
//                    Text(
//                        text = Global.text,
//                        style = style,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//            }
//        }
//
//        if (Global.tag) {
//            FlowRow(
//                horizontalArrangement = Arrangement.spacedBy(5.dp),
//                verticalArrangement = Arrangement.spacedBy((-8).dp)
//            ) {
//                val tagList = context.localConfig.getData(Constants.TAG_LIST, "")
//                if (tagList.isNotEmpty()) {
//                    tagList.split(",").forEach {
//                        Text(
//                            text = it,
//                            style = TextStyle(
//                                color = Color.White,
//                                fontSize = 10.sp,
//                                textAlign = TextAlign.Center
//                            ),
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(3.dp))
//                                .background(
//                                    color = colorResource(
//                                        id = R.color.yellow_main
//                                    )
//                                )
//                                .padding(horizontal = 5.dp, vertical = 3.dp)
//                                .widthIn(15.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CompassWatermark(modifier: Modifier, degree: Float) {
//    var rotate by remember { mutableFloatStateOf(0f) }
//    compassWatermarkCaptureController = rememberCaptureController()
//    Box(modifier = modifier
//        .onGloballyPositioned { layoutCoordinates ->
//            leftTopSize = layoutCoordinates.size
//        }
//        .rotate(degree)
//        .offset(x = (compassX.value), y = (compassY.value))
//        .capturable(compassWatermarkCaptureController!!),
//        contentAlignment = Alignment.Center) {
//        Image(
//            painter = painterResource(id = R.mipmap.ic_north_bg),
//            contentDescription = null,
//            modifier = Modifier.rotate(rotate)
//        )
//        Image(painter = painterResource(id = R.mipmap.ic_north), contentDescription = null)
//    }
//    val sensorManager = LocalContext.current.getSystemService(SENSOR_SERVICE) as SensorManager
//    val listener = object : SensorEventListener {
//        override fun onSensorChanged(event: SensorEvent) {
//
//            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
//                rotate = -event.values[0] - degree
//            }
//        }
//
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//
//        }
//
//    }
//    ComposableLifecycle { _, event ->
//        when (event) {
//            Lifecycle.Event.ON_RESUME -> {
//                sensorManager.registerListener(
//                    listener,
//                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
//                    SensorManager.SENSOR_DELAY_GAME
//                )
//            }
//
//            Lifecycle.Event.ON_PAUSE -> {
//                sensorManager.unregisterListener(listener)
//                if (::currentRecording.isInitialized && recordingState !is VideoRecordEvent.Finalize) {
//                    currentRecording.stop()
//                }
//            }
//
//            else -> {}
//        }
//    }
//}
//
//@Composable
//fun LogoWatermark(modifier: Modifier, degree: Float) {
//    if (Global.logo && Global.imageUri != null) {
//        logoWatermarkCaptureController = rememberCaptureController()
//        AsyncImage(modifier = modifier
//            .size((Global.scale * 100).dp)
//            .onGloballyPositioned { layoutCoordinates ->
//                rightTopSize = layoutCoordinates.size
//            }
//            .rotate(degree)
//            .offset(x = (logoX.value), y = (logoY.value))
//            .capturable(logoWatermarkCaptureController!!),
////        model = "https://pic.rmb.bdstatic.com/ffa3d007d8eb9015fb4a2ef20303d69b.jpeg",
////        contentDescription = null)
//            model = Global.imageUri, contentDescription = null)
//    }
//}
//
//var thumbUri = mutableStateOf<Uri?>(null)
//@Composable
//fun CameraFunctionRow(modifier: Modifier, onFaceChanged: () -> Unit) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val start = remember {
//        mutableStateOf(false)
//    }
//    val scale by animateFloatAsState(
//        targetValue = if (start.value) 0.8f else 1f,
//        animationSpec = tween(durationMillis = 200), label = ""
//    )
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Absolute.SpaceAround,
//        modifier =
//        modifier.then(
//            Modifier
//                .fillMaxWidth()
//                .padding(bottom = 15.dp)
//                .wrapContentHeight()
//        )
//    ) {
//        AsyncImage(modifier = Modifier.clickable {
//            context.startActivity(
//                Intent(Intent.ACTION_VIEW).apply {
//                    setType("image/*")
//                })
//        }.size(42.dp).clip(CircleShape), contentScale = ContentScale.Crop, model = if (thumbUri.value == null) R.mipmap.ic_thumb else thumbUri.value, placeholder = painterResource(R.mipmap.ic_thumb),contentDescription = null)
////        Image(painter = painterResource(id = R.mipmap.ic_thumb), contentDescription = null,
////            modifier = Modifier.clickable {
////
////                context.startActivity(
////                    Intent(Intent.ACTION_VIEW).apply {
////                        setType("image/*")
////                    })
////            })
//
//        val imageId = if (isVideo.value) {
//            if (!recordFinished.value) {
//                R.mipmap.ic_stop_video
//            } else R.mipmap.ic_take_video
//        } else {
//            R.mipmap.ic_take_photo
//        }
//        Image(
//            painter = painterResource(id = imageId),
//            modifier = Modifier
//                .scale(scale)
//                .clickable {
//                    if (isVideo.value) {
//                        if (!::recordingState.isInitialized ||
//                            recordingState is VideoRecordEvent.Finalize
//                        ) {
//                            startRecording()
//                        } else {
//                            currentRecording.stop()
//                        }
//                    } else {
//                        captureImage(context)
//                        start.value = true
//                        scope.launch {
//                            delay(200)
//                            start.value = false
//                            textOverlay = textWatermarkCaptureController!!
//                                .captureAsync()
//                                .await()
//                                .asAndroidBitmap()
//                                .copy(Bitmap.Config.ARGB_8888, false)
//                            if (Global.compass) {
//                                compassOverlay = compassWatermarkCaptureController!!
//                                    .captureAsync()
//                                    .await()
//                                    .asAndroidBitmap()
//                                    .copy(Bitmap.Config.ARGB_8888, false)
//                            }
//                            if (Global.logo) {
//                                logoOverlay = logoWatermarkCaptureController!!
//                                    .captureAsync()
//                                    .await()
//                                    .asAndroidBitmap()
//                                    .copy(Bitmap.Config.ARGB_8888, false)
//                            }
//                        }
//                    }
//                }.size(60.dp),
//            contentDescription = null
//        )
//        Image(
//            painter = painterResource(id = R.mipmap.ic_turn),
//            contentDescription = null,
//            modifier = Modifier.clickable { onFaceChanged() }.size(42.dp))
//    }
//}
//
//@Preview
//@Composable
//fun ModeRow() {
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 5.dp),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.clickable {
//                isVideo.value = false
//            }) {
//            Text(
//                stringResource(R.string.photo),
//                style = TextStyle(
//                    color = colorResource(if (!isVideo.value) R.color.text_222222 else R.color.text_999999),
//                    fontWeight = if (!isVideo.value) FontWeight.Bold else FontWeight.Normal
//                )
//            )
//            if (!isVideo.value) {
//                Box(
//                    Modifier
//                        .padding(vertical = 2.dp)
//                        .height(2.dp)
//                        .width(14.dp)
//                        .background(
//                            color = colorResource(R.color.yellow_main),
//                            RoundedCornerShape(2.dp)
//                        )
//                )
//            }
//        }
//        Spacer(Modifier.width(35.dp))
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier.clickable {
//                isVideo.value = true
//            }) {
//            Text(
//                stringResource(R.string.video),
//                style = TextStyle(
//                    color = colorResource(if (isVideo.value) R.color.text_222222 else R.color.text_999999),
//                    fontWeight = if (isVideo.value) FontWeight.Bold else FontWeight.Normal
//                )
//            )
//            if (isVideo.value) {
//                Box(
//                    Modifier
//                        .padding(vertical = 2.dp)
//                        .height(2.dp)
//                        .width(14.dp)
//                        .background(
//                            color = colorResource(R.color.yellow_main),
//                            RoundedCornerShape(2.dp)
//                        )
//                )
//            }
//        }
//    }
//}
//
//var selectedIndex = mutableIntStateOf(1)
//
//@Composable
//fun PhotoSpinner(navSubscribe: () -> Unit) {
//    var expanded by remember { mutableStateOf(false) }
//    val items = Constants.image_resolution_list
//    Box(
//        modifier = Modifier
//            .wrapContentSize(Alignment.TopStart)
//            .fillMaxHeight()
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .clickable(onClick = { expanded = true })
//                .fillMaxHeight()
//        ) {
//            Text(
//                items[selectedIndex.value].title,
//                style = TextStyle(color = colorResource(id = R.color.text_222222))
//            )
//            if (items[selectedIndex.value].isPro) {
//                Image(
//                    modifier = Modifier.padding(start = 3.dp),
//                    painter = painterResource(id = R.mipmap.ic_pro),
//                    contentDescription = null
//                )
//            }
//            Image(
//                modifier = Modifier.padding(start = 7.dp),
//                painter = painterResource(id = R.mipmap.ic_drop_down),
//                contentDescription = null
//            )
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier
//                .wrapContentSize()
//                .background(
//                    Color.White
//                )
//        ) {
//            items.forEachIndexed { index, s ->
//                DropdownMenuItem(onClick = {
//                    if (Global.isVip.value || !items[index].isPro) {
//                        selectedIndex.value = index
//                        expanded = false
//                        targetResolution.value =
//                            android.util.Size(items[index].height, items[index].width)
//                        ratio.value = items[index].width / items[index].height.toFloat()
//                    } else {
//                        navSubscribe()
//                    }
//                }, text = {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            items[index].title,
//                            style = TextStyle(color = colorResource(id = R.color.text_222222))
//                        )
//                        if (items[index].isPro) {
//                            Image(
//                                modifier = Modifier.padding(start = 3.dp),
//                                painter = painterResource(id = R.mipmap.ic_pro),
//                                contentDescription = null
//                            )
//                        }
//                    }
//
//                })
//            }
//        }
//    }
//}
//
//val videoSelectedIndex = mutableIntStateOf(0)
//
//@Composable
//fun VideoSpinner(navSubscribe: () -> Unit) {
//    var expanded by remember { mutableStateOf(false) }
//    val items = Constants.video_resolution_list
//
//    Box(
//        modifier = Modifier
//            .wrapContentSize(Alignment.TopStart)
//            .fillMaxHeight()
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .clickable(onClick = { expanded = true })
//                .fillMaxHeight()
//        ) {
//            Text(
//                items[videoSelectedIndex.value].title,
//                style = TextStyle(color = colorResource(id = R.color.text_222222))
//            )
//            if (items[videoSelectedIndex.value].isPro) {
//                Image(
//                    modifier = Modifier.padding(start = 3.dp),
//                    painter = painterResource(id = R.mipmap.ic_pro),
//                    contentDescription = null
//                )
//            }
//            Image(
//                modifier = Modifier.padding(start = 7.dp),
//                painter = painterResource(id = R.mipmap.ic_drop_down),
//                contentDescription = null
//            )
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier
//                .wrapContentSize()
//                .background(
//                    Color.White
//                )
//        ) {
//            items.forEachIndexed { index, s ->
//                DropdownMenuItem(onClick = {
//                    if (Global.isVip.value || !items[index].isPro) {
//                        videoSelectedIndex.value = index
//                        expanded = false
//                        videoQuality.value = when (index) {
//                            0 -> Quality.SD
//                            1 -> Quality.HD
//                            else -> Quality.FHD
//                        }
//                        videoTargetResolution.value =
//                            android.util.Size(items[index].height, items[index].width)
//                        ratio.value = items[index].width / items[index].height.toFloat()
//                    } else {
//                        navSubscribe()
//                    }
//                }, text = {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            items[index].title,
//                            style = TextStyle(color = colorResource(id = R.color.text_222222))
//                        )
//                        if (items[index].isPro) {
//                            Image(
//                                modifier = Modifier.padding(start = 3.dp),
//                                painter = painterResource(id = R.mipmap.ic_pro),
//                                contentDescription = null
//                            )
//                        }
//                    }
//
//                })
//            }
//        }
//    }
//}
//
//@Composable
//fun FlashPanel() {
//    var expanded by remember { mutableStateOf(false) }
//    Box(
//        modifier = Modifier
//            .wrapContentSize(Alignment.TopStart)
//            .fillMaxHeight()
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .clickable(onClick = { expanded = true })
//                .fillMaxHeight()
//        ) {
//            val imageId = when (flashMode.value) {
//                FLASH_MODE_AUTO -> R.mipmap.ic_flash_auto
//                FLASH_MODE_ON -> R.mipmap.ic_flash_on
//                FLASH_MODE_OFF -> R.mipmap.ic_flash_off
//                else -> R.mipmap.ic_flash_auto
//            }
//            Image(
//                modifier = Modifier.padding(start = 17.dp),
//                painter = painterResource(id = imageId),
//                contentDescription = null
//            )
//        }
//
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false },
//            modifier = Modifier
//                .wrapContentSize()
//                .background(
//                    Color.White
//                )
//        ) {
//            DropdownMenuItem(onClick = {}, text = {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceAround,
//                    modifier = Modifier.width(120.dp)
//                ) {
//                    Image(
//                        modifier = Modifier
//                            .clickable {
//                                expanded = false
//                                flashMode.value = FLASH_MODE_AUTO
//                                imageCapture.flashMode = flashMode.value
//                            },
//                        painter = painterResource(id = R.mipmap.ic_flash_auto),
//                        contentDescription = null
//                    )
//                    Image(
//                        modifier = Modifier
//                            .clickable {
//                                expanded = false
//                                flashMode.value = FLASH_MODE_ON
//                                imageCapture.flashMode = flashMode.value
//                            },
//                        painter = painterResource(id = R.mipmap.ic_flash_on),
//                        contentDescription = null
//                    )
//                    Image(
//                        modifier = Modifier
//                            .clickable {
//                                expanded = false
//                                flashMode.value = FLASH_MODE_OFF
//                                imageCapture.flashMode = flashMode.value
//                            },
//                        painter = painterResource(id = R.mipmap.ic_flash_off),
//                        contentDescription = null
//                    )
//                }
//
//            })
//        }
//    }
//}
//
//var cameraSize = IntSize(0, 0)
//
//@Composable
//fun CameraPreviewScreen(modifier: Modifier) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    previewView = remember { PreviewView(context) }
//    LaunchedEffect(
//        isVideo.value,
//        lensFacing.value,
//        flashMode.value,
//        targetResolution.value,
//        videoQuality.value,
//        videoTargetResolution
//    ) {
//        switchMode(context = context, lifecycleOwner = lifecycleOwner, previewView = previewView)
//    }
//    AndroidView(
//        factory = { previewView }, modifier = modifier.then(
//            Modifier
//                .fillMaxWidth()
//                .onGloballyPositioned { layoutCoordinates -> cameraSize = layoutCoordinates.size }
//                .aspectRatio(
//                    ratio.value
//                )
//        )
//    )
//}
//
//private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
//    suspendCoroutine { continuation ->
//        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
//            cameraProvider.addListener({
//                continuation.resume(cameraProvider.get())
//            }, ContextCompat.getMainExecutor(this))
//        }
//    }
//
//fun Bitmap.rotate(degrees: Float): Bitmap {
//    val matrix = Matrix().apply { postRotate(degrees) }
//    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
//}
//
//var imageCapture = ImageCapture.Builder().build()
//
//private fun captureImage(context: Context) {
//    imageCapture.takePicture(ContextCompat.getMainExecutor(context), object :
//        ImageCapture.OnImageCapturedCallback() {
//        override fun onCaptureSuccess(image: ImageProxy) {
//            super.onCaptureSuccess(image)
//            Logger.e("${image.imageInfo.rotationDegrees}")
////            val bitmap = Utils.imageProxyToBitmap(image)
//            val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees.toFloat())
//
//            bitmap.let {
//                Logger.e("bitmap--width:${it.width}, height:${it.height}")
//                val isLandscape = it.width > it.height
//
////                it.saveToAlbum(context, "test.jpg")
//
//                val scaleBitmap = if (isLandscape && it.width != targetResolution.value.height) {
//                    it.scale(targetResolution.value.height, targetResolution.value.width)
//                } else if (!isLandscape && it.width != targetResolution.value.width) {
//                    it.scale(targetResolution.value.width, targetResolution.value.height)
//                } else it
////                    val scaleBitmap = it
//                generateWatermarkPicture(mutableListOf<WatermarkImage>(), isLandscape, scaleBitmap)
//            }
//        }
//    })
//}
//
//lateinit var currentRecording: Recording
//private lateinit var recordingState: VideoRecordEvent
//var recordFinished = mutableStateOf(true)
//var videoInterval: Interval = Interval(1, TimeUnit.SECONDS)
//var recordTime = mutableStateOf("00:00:00")
//
//private fun startRecording() {
//    // create MediaStoreOutputOptions for our recorder: resulting our recording!
//    val name = Constants.VIDEO_PREFIX_DEFAULT +
//            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
//                .format(System.currentTimeMillis()) + ".mp4"
//    val contentValues = ContentValues().apply {
//        put(MediaStore.Video.Media.DISPLAY_NAME, name)
//    }
//    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
//        context.contentResolver,
//        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//    )
//        .setContentValues(contentValues)
//        .build()
//
//    // configure Recorder and Start recording to the mediaStoreOutput.
//    currentRecording = videoCapture.output
//        .prepareRecording(context, mediaStoreOutput)
////        .apply { if (audioEnabled) withAudioEnabled() }
//        .start(
//            ContextCompat.getMainExecutor(context)
//        ) { event ->
//
//            if (event !is VideoRecordEvent.Status)
//                recordingState = event
//            if (event is VideoRecordEvent.Start) {
//                Logger.e("Recording started")
//                recordFinished.value = false
//                videoInterval.reset()
//                videoInterval.subscribe {
//                    recordTime.value = Utils.formatVideoRecordTime(it)
//                }.start()
//            }
//            if (event is VideoRecordEvent.Finalize) {
//                recordFinished.value = true
//                videoInterval.cancel()
////                thumbUri.value = event.outputResults.outputUri
//                val msg = "Video saved: ${event.outputResults.outputUri}"
//                Logger.e(msg)
//            }
//        }
//
//}
//
//var isVideo = mutableStateOf(false)
//lateinit var previewView: PreviewView
//lateinit var videoCapture: VideoCapture<Recorder>
//var cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing.intValue).build()
//private suspend fun switchMode(
//    context: Context,
//    lifecycleOwner: LifecycleOwner,
//    previewView: PreviewView
//) {
//    Logger.e("switchMode")
//    if (isVideo.value) {
//        ratio.value =
//            videoTargetResolution.value.height.toFloat() / videoTargetResolution.value.width.toFloat()
//    } else {
//        ratio.value =
//            targetResolution.value.height.toFloat() / targetResolution.value.width.toFloat()
//    }
//    val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
//        if (ratio.value == 3 / 4f)
//            AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY else AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
//    ).build()
//    val preview =
//        androidx.camera.core.Preview.Builder().setResolutionSelector(resolutionSelector).build()
//    cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing.value).build()
//    if (isVideo.value) {
//        val recorder = Recorder.Builder()
//            .setQualitySelector(QualitySelector.from(videoQuality.value))
//            .build()
//        videoCapture = VideoCapture.withOutput(recorder)
//
//    } else {
//        imageCapture = ImageCapture.Builder().setFlashMode(flashMode.value)
//            .setResolutionSelector(resolutionSelector).build()
//    }
//    val cameraProvider = context.getCameraProvider()
//    cameraProvider.unbindAll()
//    if (isVideo.value) {
//        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, videoCapture)
//    } else {
//        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
//    }
//    preview.surfaceProvider = previewView.surfaceProvider
//}
//
//var px10 = 10.0
//var textOverlay: Bitmap? = null
//var compassOverlay: Bitmap? = null
//var logoOverlay: Bitmap? = null
//private fun getWaterMark(isLandscape: Boolean): WatermarkImage? {
//    if (textOverlay == null) return null
//    textOverlay?.let {
//        Logger.e("textOverlay--width:${it.width}--height:${it.height}")
//    }
//
//    val tempHeight = (px10 * 2 + leftBottomSize.height)
//
//    val scaleX = if (isLandscape) {
//        px10 / cameraSize.height
//    } else {
//        px10 / cameraSize.width
//    }
//    val scaleY =
//        if (isLandscape) {
//            if (tempHeight > cameraSize.width) {
//                px10 / cameraSize.width
//            } else {
//                1 - ((px10 + leftBottomSize.height) / cameraSize.width)
//            }
//        } else {
//            if (tempHeight > cameraSize.height) {
//                px10 / cameraSize.height
//            } else {
//                1 - ((px10 + leftBottomSize.height) / cameraSize.height)
//            }
//        }
//    val imageMark = WatermarkImage(textOverlay).apply {
//        val scale = if (isLandscape) {
//            leftBottomSize.width.toDouble() / cameraSize.height
//        } else {
//            leftBottomSize.width.toDouble() / cameraSize.width
//        }
//        position = WatermarkPosition(scaleX, scaleY)
//        setImageAlpha(255)
//        size = scale
//        Logger.e("textOverlay--scaleX:$scaleX, scaleY:$scaleY, size:$scale")
//    }
//    return imageMark
//}
//
//private fun getNorthWaterMark(isLandscape: Boolean): WatermarkImage? {
//    if (compassOverlay == null || !Global.compass) return null
//    compassOverlay?.let {
//        Logger.e("compassOverlay--width:${it.width}--height:${it.height}--${it.rowBytes}--${it.isRecycled}")
//    }
//    val scaleX = if (isLandscape) {
//        px10 / cameraSize.height
//    } else {
//        px10 / cameraSize.width
//    }
//    val scaleY =
//        if (isLandscape) {
//            px10 / cameraSize.width
//        } else {
//            px10 / cameraSize.height
//        }
//    val imageMark = WatermarkImage(compassOverlay).apply {
//        val scale = if (isLandscape) {
//            leftTopSize.width.toDouble() / cameraSize.height
//        } else {
//            leftTopSize.width.toDouble() / cameraSize.width
//        }
//        position = WatermarkPosition(scaleX, scaleY)
//        setImageAlpha(255)
//        size = scale
//        Logger.e("compassOverlay--scaleX:$scaleX, scaleY:$scaleY, size:$scale")
//    }
//    return imageMark
//}
//
//private fun getImageWaterMark(isLandscape: Boolean): WatermarkImage? {
//    if (logoOverlay == null || !Global.logo) return null
//    logoOverlay?.let {
//        Logger.e("logoOverlay--width:${it.width}--height:${it.height}")
//    }
//    val scaleX = if (isLandscape) {
//        (cameraSize.height - rightTopSize.width - px10) / cameraSize.height
//    } else {
//        (cameraSize.width - rightTopSize.width - px10) / cameraSize.width
//    }
//    val scaleY =
//        if (isLandscape) {
//            px10 / cameraSize.width
//        } else {
//            px10 / cameraSize.height
//        }
//    val imageMark = WatermarkImage(logoOverlay).apply {
//        val scale = if (isLandscape) {
//            rightTopSize.width.toDouble() / cameraSize.height
//        } else {
//            rightTopSize.width.toDouble() / cameraSize.width
//        }
//        position = WatermarkPosition(scaleX, scaleY)
//        setImageAlpha(255)
//        size = scale
//        Logger.e("logoOverlay--scaleX:$scaleX, scaleY:$scaleY, size:$scale")
//    }
//    return imageMark
//}
////
////fun getMapWaterMark(isLandscape: Boolean, view: View, overlay: Bitmap?): WatermarkImage? {
////    if (context == null || !view.isShown || view.width == 0 || overlay == null) return null
////
////    val scaleX = if (isLandscape) {
////        1 - (view.width.toDouble() / cameraSize.height)
////    } else {
////        1 - (view.width.toDouble() / cameraSize.width)
////    }
////    val scaleY =
////        if (isLandscape) {
////            1 - (view.height.toDouble() / cameraSize.width)
////        } else {
////            1 - (view.height.toDouble() / cameraSize.height)
////        }
////    val imageMark = WatermarkImage(overlay).apply {
////        val scale = if (isLandscape) {
////            view.width.toDouble() / cameraSize.height
////        } else {
////            view.width.toDouble() / cameraSize.width
////        }
////        position = WatermarkPosition(scaleX, scaleY)
////        setImageAlpha(255)
////        size = scale
////    }
////    return imageMark
////}
//
//lateinit var context: Context
//private fun generateWatermarkPicture(
//    markList: MutableList<WatermarkImage>,
//    isLandscape: Boolean,
//    bgImage: Bitmap
//) {
//
////    getImageWaterMark(isLandscape, binding.imageWatermark)?.let {
////        markList.add(it)
////    }
//    Logger.e("cameraOverlay--width:${cameraSize.width}--height:${cameraSize.height}")
//    getWaterMark(isLandscape)?.let {
//        markList.add(it)
//    }
//    getNorthWaterMark(isLandscape)?.let {
//        markList.add(it)
//    }
//    getImageWaterMark(isLandscape)?.let {
//        markList.add(it)
//    }
//    if (markList.isEmpty()) {
//        thumbUri.value = bgImage.saveToAlbum(
//            context,
//            "${
//                context.localConfig.getData(
//                    Constants.PHOTO_PREFIX,
//                    Constants.PHOTO_PREFIX_DEFAULT
//                )
//            }_${System.currentTimeMillis()}.jpg",
//            context.localConfig.getData(
//                Constants.CUSTOM_DIR,
//                Constants.CUSTOM_DIR_DEFAULT
//            )
//        )
////            binding.thumb.setImageURI(tempUri)
////        query()
//    } else {
//        Logger.e("save")
//        val bitmapResult = WatermarkBuilder
//            .create(context, bgImage)
//            .loadWatermarkImages(markList)
//            .watermark
//            .outputImage
//        thumbUri.value = bitmapResult.saveToAlbum(
//            context,
//            "${
//                context.localConfig.getData(
//                    Constants.PHOTO_PREFIX,
//                    Constants.PHOTO_PREFIX_DEFAULT
//                )
//            }_${System.currentTimeMillis()}.jpg",
//            context.localConfig.getData(
//                Constants.CUSTOM_DIR,
//                Constants.CUSTOM_DIR_DEFAULT
//            )
//        )
////            binding.thumb.setImageURI(tempUri)
////        query()
//    }
//}
//
//var density: Float = 1f
//val textX = mutableStateOf(0.dp)
//val textY = mutableStateOf(0.dp)
//val compassX = mutableStateOf(0.dp)
//val compassY = mutableStateOf(0.dp)
//val logoX = mutableStateOf(0.dp)
//val logoY = mutableStateOf(0.dp)
//
//@Composable
//fun DeviceOrientationListener(applicationContext: Context, degrees: MutableState<Float>) {
//
//    density = LocalDensity.current.density
//    px10 = (density * 10).toDouble()
//    DisposableEffect(Unit) {
//
//        val orientationEventListener = object : OrientationEventListener(applicationContext) {
//            override fun onOrientationChanged(orientation: Int) {
//                if (orientation == ORIENTATION_UNKNOWN) {
//                    return
//                }
//                val rotation = when (orientation) {
//                    in 45 until 135 -> Surface.ROTATION_270
//                    in 135 until 225 -> Surface.ROTATION_180
//                    in 225 until 315 -> Surface.ROTATION_90
//                    else -> Surface.ROTATION_0
//                }
//                if (imageCapture.targetRotation != rotation) {
//                    imageCapture.targetRotation = rotation
//                    degrees.value = rotation * 90f
//                    textX.value = textOffsetX(degrees.value)
//                    textY.value = textOffsetY(degrees.value)
////                    Logger.e("X:${textX.value}, Y:${textY.value}")
//                    compassX.value = compassOffsetX(degrees.value)
//                    compassY.value = compassOffsetY(degrees.value)
////                    Logger.e("X:${compassX.value}, Y:${compassY.value}")
//                    logoX.value = logoOffsetX(degrees.value)
//                    logoY.value = logoOffsetY(degrees.value)
//                    Logger.e("X:${logoX.value}, Y:${logoY.value}")
//                }
//            }
//        }
//        orientationEventListener.enable()
//
//        // Disable the event onDispose
//        onDispose {
//            orientationEventListener.disable()
//        }
//
//    }
//}
//
//@Composable
//fun ComposableLifecycle(
//    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
//    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
//) {
//
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { source, event ->
//            onEvent(source, event)
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//}
//
//private fun getAddressInfo(
//    context: Context,
//    lat: Double,
//    lng: Double,
//    result: MutableState<String>
//) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        Geocoder(context).getFromLocation(
//            lat,
//            lng,
//            5
//        ) { addresses ->
//            if (addresses.isEmpty()) return@getFromLocation
//            Logger.e(addresses[0].toString())
//            if (addresses[0].featureName == null) {
//                result.value = addresses[0].getAddressLine(0)
//            } else {
//                result.value = addresses[0].featureName
//            }
//        }
//    } else {
//        try {
//            val addresses = Geocoder(context).getFromLocation(
//                lat,
//                lng,
//                5
//            )
//            if (!addresses.isNullOrEmpty()) {
//                if (addresses[0].featureName == null) {
//                    result.value = addresses[0].getAddressLine(0)
//                } else {
//                    result.value = addresses[0].featureName
//                }
//                Logger.e(addresses[0].toString())
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
//
//private fun getWeather(lat: Double, lng: Double, weatherStr: MutableState<String>) {
//    val languageCode = Locale.getDefault().language
//    scopeNet {
//        val result =
//            Get<String>("http://api.weatherapi.com/v1/current.json?key=d7dc671cb791421aaf930024240107&q=$lat,$lng&lang=$languageCode").await()
//        try {
//            val obj = JSONObject(result).optJSONObject("current").optJSONObject("condition")
//            obj.optString("text")
//            obj.optString("icon")
//            Logger.e(obj.toString())
//            weatherStr.value = obj.optString("text")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
//
//
//
