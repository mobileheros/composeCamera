package com.mobileheros.gpscamera.ui.camera

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.drake.net.Get
import com.drake.net.time.Interval
import com.drake.net.utils.runMain
import com.drake.net.utils.scopeNetLife
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.mobileheros.gpscamera.R
import com.mobileheros.gpscamera.bean.ImageBean
import com.mobileheros.gpscamera.databinding.FragmentCameraBinding
import com.mobileheros.gpscamera.dialog.RateDialog
import com.mobileheros.gpscamera.event.WatermarkChangedEvent
import com.mobileheros.gpscamera.utils.CommonUtils
import com.mobileheros.gpscamera.utils.CommonUtils.safeNavigate
import com.mobileheros.gpscamera.utils.Constants
import com.mobileheros.gpscamera.utils.Global
import com.mobileheros.gpscamera.utils.ImageSaveUtils.saveToAlbum
import com.mobileheros.gpscamera.utils.MediaStoreUtils
import com.mobileheros.gpscamera.utils.VideoSaveUtils.copyToAlbum
import com.mobileheros.gpscamera.utils.getData
import com.mobileheros.gpscamera.utils.localConfig
import com.mobileheros.gpscamera.utils.putData
import com.orhanobut.logger.Logger
import com.mobileheros.gpscamera.utils.Global.isVideo
import com.mobileheros.gpscamera.utils.PlayBillingHelper
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import com.watermark.androidwm_light.WatermarkBuilder
import com.watermark.androidwm_light.bean.WatermarkImage
import com.watermark.androidwm_light.bean.WatermarkPosition
import com.zackratos.ultimatebarx.ultimatebarx.java.UltimateBarX
import com.zackratos.ultimatebarx.ultimatebarx.navigationBar
import com.zackratos.ultimatebarx.ultimatebarx.statusBar
import kotlinx.coroutines.Dispatchers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Method
import java.util.Locale
import java.util.concurrent.TimeUnit


class CameraFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCameraBinding
    private var tempUri: Uri? = null
    private lateinit var interval: Interval
    private lateinit var manager: SensorManager
    private var px10: Double = 10.0
    private var px20: Double = 20.0
    var videoInterval: Interval = Interval(1, TimeUnit.SECONDS)
    private var curOrientation = 0
    private lateinit var locationCallback: LocationCallback


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.e("onCreateView: $this")
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        val root: View = binding.root
        px10 = CommonUtils.dp2px(requireContext(), 10f).toDouble()
        px20 = px10 * 2
        binding.turn.setOnClickListener(this)
        binding.takePicture.setOnClickListener(this)
        binding.thumb.setOnClickListener(this)
        binding.light.setOnClickListener(this)
        binding.lightAuto.setOnClickListener(this)
        binding.lightOn.setOnClickListener(this)
        binding.lightOff.setOnClickListener(this)
        binding.tag.setOnClickListener(this)
        binding.captureMode.setOnClickListener(this)
        binding.videoMode.setOnClickListener(this)
        binding.proMode.setOnClickListener(this)
        if (Global.dateFormat == getString(R.string.display_off)) {
            binding.time.visibility = GONE
        } else {
            binding.time.visibility = VISIBLE
            binding.time.text = CommonUtils.formatTime(Global.dateFormat, System.currentTimeMillis())
            interval = Interval(1, TimeUnit.SECONDS).subscribe {
                if (context != null) {
                    binding.time.text =
                        CommonUtils.formatTime(Global.dateFormat, System.currentTimeMillis())
                }
            }.start()
        }
        addMap()
        manager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Logger.d(locationResult.locations)
                if (locationResult.locations.isNotEmpty()) {
                    locationUiTask(locationResult.locations[0])
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
                Logger.d(p0)
            }
        }
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.e("onViewCreated: $this")
        statusBar {
            color = Color.WHITE
            light = true
        }
        navigationBar {
            color = Color.WHITE
        }
        setupCamera()
        switchMode(Global.isVideo)
        getLocalConfig()
        setupWatermark()
        initPictureSpinner()
        initVideoSpinner()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermission()

//        if (!XXPermissions.isGranted(requireContext(), Permission.READ_MEDIA_IMAGES)) {
//            XXPermissions.with(this).permission(Permission.READ_MEDIA_IMAGES).request(null)
//        }
        EventBus.getDefault().register(this)
        query()

//        updateBottomView()
    }

    private fun updateBottomView() {
        binding.modeLayout.postDelayed({
            val viewHeight = binding.middleLayout.height + binding.modeLayout.height
            val screenHeight = resources.displayMetrics.heightPixels
            val navBarHeight = UltimateBarX.getNavigationBarHeight()
            val enough = screenHeight - navBarHeight >= viewHeight
            if (enough) {
                ConstraintSet().also {
                    it.clone(binding.rootView)
                    it.clear(binding.bottomPanel.id, ConstraintSet.TOP)
                    it.clear(binding.bottomPanel.id, ConstraintSet.BOTTOM)
                    it.connect(
                        binding.bottomPanel.id,
                        ConstraintSet.BOTTOM,
                        binding.middleLayout.id,
                        ConstraintSet.BOTTOM
                    )

                    it.clear(binding.modeLayout.id, ConstraintSet.BOTTOM)
                    it.clear(binding.modeLayout.id, ConstraintSet.TOP)
                    it.connect(
                        binding.modeLayout.id,
                        ConstraintSet.TOP,
                        binding.middleLayout.id,
                        ConstraintSet.BOTTOM
                    )
                    it.applyTo(binding.rootView)
                }
                binding.modeLayout.alpha = 1f
                binding.bottomPanel.background.alpha = 1
            } else {
                ConstraintSet().also {
                    it.clone(binding.rootView)
                    it.clear(binding.modeLayout.id, ConstraintSet.BOTTOM)
                    it.clear(binding.modeLayout.id, ConstraintSet.TOP)
                    it.connect(
                        binding.modeLayout.id,
                        ConstraintSet.BOTTOM,
                        binding.middleLayout.id,
                        ConstraintSet.BOTTOM
                    )

                    it.clear(binding.bottomPanel.id, ConstraintSet.TOP)
                    it.clear(binding.bottomPanel.id, ConstraintSet.BOTTOM)
                    it.connect(
                        binding.bottomPanel.id,
                        ConstraintSet.BOTTOM,
                        binding.modeLayout.id,
                        ConstraintSet.TOP
                    )
                    it.applyTo(binding.rootView)
                }
                binding.modeLayout.alpha = 0.5f
                binding.bottomPanel.background.alpha = 0
            }
            Logger.e("UltimateBarX.getNavigationBarHeight()===${UltimateBarX.getNavigationBarHeight()}")
            Logger.e("viewHeight:$viewHeight=====screenHeight$screenHeight=====root:${binding.root.height}")
            Logger.e("space enough ?${screenHeight - navBarHeight > viewHeight}")
        }, 1000)
    }

    private fun getLocalConfig() {
        with(requireContext().localConfig) {
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
                if (Global.localVip) this.getData(Constants.SWITCH_LOGO, false) else false
            Global.scale = this.getData(Constants.SCALE, 0.5f)
        }
    }

    private fun setupWatermark() {
        if (context == null) return
        binding.time.visibility = if (Global.dateFormat.isEmpty()) GONE else VISIBLE
        binding.location.visibility = if (Global.gps) VISIBLE else GONE
        binding.address.visibility =
            if (Global.address) VISIBLE else GONE
        binding.altitude.visibility =
            if (Global.altitude) VISIBLE else GONE
        binding.weatherLayout.visibility =
            if (Global.weather) VISIBLE else GONE
        binding.text.visibility = if (Global.textSwitch) VISIBLE else GONE
        binding.text.text = Global.text
        binding.chipLayout.visibility = if (Global.tag) VISIBLE else GONE
        if (Global.gps || Global.address || Global.altitude || Global.weather || Global.textSwitch || Global.tag || Global.dateFormat.isNotEmpty()) {
            binding.watermarkLayout.visibility = VISIBLE
        } else {
            binding.watermarkLayout.visibility = GONE
        }
        addTag()

        binding.imageWatermark.visibility = if (Global.logo) VISIBLE else GONE
        binding.imageWatermark.apply {
            (this.layoutParams as ConstraintLayout.LayoutParams).height =
                (CommonUtils.dp2px(requireContext(), 128f) * Global.scale).toInt()
        }
        Logger.e("imageUri--${Global.imageUri}")
        Glide.with(requireContext()).load(Global.imageUri).into(binding.imageWatermark)

        binding.mapLayout.visibility = if (Global.map) VISIBLE else GONE
        binding.northLayout.visibility = if (Global.compass) VISIBLE else GONE
    }

    private fun addTag() {
        if (context == null) return
        val result =
            requireContext().localConfig.getData(Constants.TAG_LIST, "").split(",").toMutableList()
        binding.chipLayout.removeAllViews()
        for (i in result.indices) {
            val chip1 = TextView(requireContext()).apply {
                text = result[i]
                setTextColor(resources.getColor(R.color.text_black, null))
                background = ResourcesCompat.getDrawable(resources, R.drawable.button_bg_5, null)
                val padValue = CommonUtils.dp2px(requireContext(), 3f)
                setPadding(padValue * 2, padValue, padValue * 2, padValue)
                setSingleLine()
            }
            binding.chipLayout.addView(chip1)
        }
    }

    private fun getWaterMark(isLandscape: Boolean, view: View): WatermarkImage? {
        if (context == null || !view.isShown) return null
        Logger.e("watermark--width:${view.width}, height:${view.height}")
        Logger.e("camera--width:${binding.camera.width}, height:${binding.camera.height}")
        val overlay =
            CommonUtils.getBitmapFromViewUsingCanvas(view)
        val tempHeight = (px20 + view.height)

        val scaleX = if (isLandscape) {
            px10 / binding.camera.height
        } else {
            px10 / binding.camera.width
        }
        val scaleY =
            if (isLandscape) {
                if (tempHeight > binding.camera.width) {
                    px10 / binding.camera.width
                } else {
                    1 - ((px10 + view.height) / binding.camera.width)
                }
            } else {
                if (tempHeight > binding.camera.height) {
                    px10 / binding.camera.height
                } else {
                    1 - ((px10 + view.height) / binding.camera.height)
                }
            }
        val imageMark = WatermarkImage(overlay).apply {
            val scale = if (isLandscape) {
                view.width.toDouble() / binding.camera.height
            } else {
                view.width.toDouble() / binding.camera.width
            }
            position = WatermarkPosition(scaleX, scaleY)
            setImageAlpha(255)
            size = scale
            Logger.e("watermark--scaleX:$scaleX, scaleY:$scaleY, size:$scale")
        }
        return imageMark
    }

    private fun getNorthWaterMark(isLandscape: Boolean): WatermarkImage? {
        if (context == null || !Global.compass) return null
        val overlay =
            CommonUtils.getBitmapFromViewUsingCanvas(binding.northLayout)
        val scaleX = if (isLandscape) {
            px10 / binding.camera.height
        } else {
            px10 / binding.camera.width
        }
        val scaleY =
            if (isLandscape) {
                px10 / binding.camera.width
            } else {
                px10 / binding.camera.height
            }
        val imageMark = WatermarkImage(overlay).apply {
            val scale = if (isLandscape) {
                binding.northLayout.width.toDouble() / binding.camera.height
            } else {
                binding.northLayout.width.toDouble() / binding.camera.width
            }
            position = WatermarkPosition(scaleX, scaleY)
            setImageAlpha(255)
            size = scale
        }
        return imageMark
    }

    private fun getImageWaterMark(isLandscape: Boolean, view: View): WatermarkImage? {
        if (context == null || !view.isShown || view.width == 0) return null
        val overlay =
            CommonUtils.getBitmapFromViewUsingCanvas(view)
        val scaleX = if (isLandscape) {
            1 - ((px10
                    + view.width) / binding.camera.height)
        } else {
            1 - ((px10
                    + view.width) / binding.camera.width)
        }
        val scaleY =
            if (isLandscape) {
                px10 / binding.camera.width
            } else {
                px10 / binding.camera.height
            }
        val imageMark = WatermarkImage(overlay).apply {
            val scale = if (isLandscape) {
                view.width.toDouble() / binding.camera.height
            } else {
                view.width.toDouble() / binding.camera.width
            }
            position = WatermarkPosition(scaleX, scaleY)
            setImageAlpha(128)
            size = scale
        }
        return imageMark
    }

    fun getMapWaterMark(isLandscape: Boolean, view: View, overlay: Bitmap?): WatermarkImage? {
        Logger.e("getMapWaterMark${view.isShown}--${view.width}--${overlay==null}")
        if (context == null || !view.isShown || view.width == 0 || overlay == null) return null

        val scaleX = if (isLandscape) {
            1 - (view.width.toDouble() / binding.camera.height)
        } else {
            1 - (view.width.toDouble() / binding.camera.width)
        }
        val scaleY =
            if (isLandscape) {
                1 - (view.height.toDouble() / binding.camera.width)
            } else {
                1 - (view.height.toDouble() / binding.camera.height)
            }
        val imageMark = WatermarkImage(overlay).apply {
            val scale = if (isLandscape) {
                view.width.toDouble() / binding.camera.height
            } else {
                view.width.toDouble() / binding.camera.width
            }
            position = WatermarkPosition(scaleX, scaleY)
            setImageAlpha(255)
            size = scale
        }
        return imageMark
    }

    private fun generateWatermarkPicture(
        markList: MutableList<WatermarkImage>,
        isLandscape: Boolean,
        bgImage: Bitmap
    ) {
        if (context == null) return
        getNorthWaterMark(isLandscape)?.let {
            markList.add(it)
        }
        getImageWaterMark(isLandscape, binding.imageWatermark)?.let {
            markList.add(it)
        }
        getWaterMark(isLandscape, binding.watermarkLayout)?.let {
            markList.add(it)
        }
        if (markList.isEmpty()) {
            tempUri = bgImage.saveToAlbum(
                requireContext(),
                "${
                    requireContext().localConfig.getData(
                        Constants.PHOTO_PREFIX,
                        Constants.PHOTO_PREFIX_DEFAULT
                    )
                }_${System.currentTimeMillis()}.jpg",
                requireContext().localConfig.getData(
                    Constants.CUSTOM_DIR,
                    Constants.CUSTOM_DIR_DEFAULT
                )
            )
//            binding.thumb.setImageURI(tempUri)
            query()
        } else {
            val bitmapResult = WatermarkBuilder
                .create(requireContext(), bgImage)
                .loadWatermarkImages(markList)
                .watermark
                .outputImage
            tempUri = bitmapResult.saveToAlbum(
                requireContext(),
                "${
                    requireContext().localConfig.getData(
                        Constants.PHOTO_PREFIX,
                        Constants.PHOTO_PREFIX_DEFAULT
                    )
                }_${System.currentTimeMillis()}.jpg",
                requireContext().localConfig.getData(
                    Constants.CUSTOM_DIR,
                    Constants.CUSTOM_DIR_DEFAULT
                )
            )
//            binding.thumb.setImageURI(tempUri)
            query()
        }
    }

    private fun setupCamera() {
        binding.camera.apply {
            setLifecycleOwner(viewLifecycleOwner)
            addCameraListener(object : CameraListener() {
                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)
                    Logger.e("onPictureTaken")
                    if (context == null) return
                    val markList = mutableListOf<WatermarkImage>()
                    result.toBitmap {
                        it?.let {
                            Logger.e("bitmap--width:${it.width}, height:${it.height}")
                            Logger.e("rotation: ${result.rotation}")
                            val isLandscape = it.width > it.height

                            val scaleBitmap = if (it.width != targetWidth) {
                                it.scale(targetWidth, targetHeight)
                            } else it

                            Logger.e("${Global.map}--${mMap==null}--$isMapLoaded--${this@CameraFragment.location}")
                            if (Global.map && mMap != null && isMapLoaded && this@CameraFragment.location != null) {
                                getMapWaterMark(
                                    isLandscape,
                                    binding.mapImage,
                                    CommonUtils.getBitmapFromViewUsingCanvas(binding.mapImage)
                                )?.let { mark ->
                                    Logger.e("add map mark")
                                    markList.add(mark)
                                }
                                generateWatermarkPicture(markList, isLandscape, scaleBitmap)
                            } else {
                                generateWatermarkPicture(markList, isLandscape, scaleBitmap)
                            }
                        }
                    }
                    if (context != null) {
                        if (!requireContext().localConfig.getData(Constants.FIRST_PHOTO, false)) {
                            Global.firstPhoto = true
                        }
                    }
                }

                override fun onVideoTaken(result: VideoResult) {
                    super.onVideoTaken(result)
                    Logger.e(Gson().toJson(result, VideoResult::class.java))
                    result.file.copyToAlbum(
                        requireContext(),
                        "${
                            requireContext().localConfig.getData(
                                Constants.VIDEO_PREFIX,
                                Constants.VIDEO_PREFIX_DEFAULT
                            )
                        }_${System.currentTimeMillis()}.mp4",
                        requireContext().localConfig.getData(
                            Constants.CUSTOM_DIR,
                            Constants.CUSTOM_DIR_DEFAULT
                        )
                    )
                }

                override fun onVideoRecordingStart() {
                    super.onVideoRecordingStart()
                    isRecording = true
                    binding.takePicture.setImageResource(R.mipmap.ic_stop_video)
                    binding.videoDuration.text = ""
                    binding.videoDuration.visibility = VISIBLE
                    binding.resolutionLayout.visibility = GONE
                    videoInterval.reset()
                    videoInterval.subscribe {
                        if (context != null) {
                            binding.videoDuration.text =
                                CommonUtils.formatDuration(Global.timeFormat, it * 1000)
                        }
                    }.start()
                }

                override fun onVideoRecordingEnd() {
                    super.onVideoRecordingEnd()
                    isRecording = false
                    binding.takePicture.setImageResource(R.mipmap.ic_take_video)
                    binding.videoDuration.text = ""
                    binding.videoDuration.visibility = GONE
                    binding.resolutionLayout.visibility = VISIBLE
                }

                override fun onOrientationChanged(orientation: Int) {
                    super.onOrientationChanged(orientation)
                    if (context == null) return
                    Logger.e("orientation:$orientation")
                    curOrientation = orientation
                    binding.watermarkLayout.pivotX = 0f
                    binding.watermarkLayout.pivotY =
                        binding.watermarkLayout.height.toFloat()
                    binding.northLayout.pivotX = CommonUtils.dp2px(requireContext(), 34f).toFloat()
                    binding.northLayout.pivotY = CommonUtils.dp2px(requireContext(), 34f).toFloat()
                    binding.imageWatermark.pivotX = 0f
                    binding.imageWatermark.pivotY = 0f
                    binding.mapLayout.pivotX = binding.mapLayout.width.toFloat() / 2
                    binding.mapLayout.pivotY = binding.mapLayout.height.toFloat() / 2
                    when (orientation) {
                        0 -> {
                            binding.watermarkLayout.apply {
                                rotation = 0f
                                translationX = 0f
                                translationY = 0f
                            }
                            binding.northLayout.apply {
                                rotation = 0f
                                translationX = 0f
                                translationY = 0f
                            }
                            binding.imageWatermark.apply {
                                rotation = 0f
                                translationX = 0f
                                translationY = 0f
                            }
                            binding.mapLayout.apply {
                                rotation = 0f
                                translationX = 0f
                                translationY = 0f
                            }
                        }

                        90 -> {
                            binding.watermarkLayout.apply {
                                rotation = 270f
                                translationX =
                                    if (binding.watermarkLayout.height + px20.toFloat() > binding.camera.width
                                    ) {
                                        binding.watermarkLayout.height.toFloat()
                                    } else {
                                        binding.camera.width.toFloat() - px20.toFloat()
                                    }
                                translationY = 0f
                            }
                            binding.northLayout.apply {
                                rotation = 270f
                                translationX = 0f
                                translationY = binding.camera.height - CommonUtils.dp2px(
                                    requireContext(),
                                    68 + 20f
                                ).toFloat()
                            }
                            binding.imageWatermark.apply {
                                rotation = 270f
                                translationX =
                                    -(binding.camera.width - binding.imageWatermark.width - px20).toFloat()
                                translationY = binding.imageWatermark.width.toFloat()
                            }
                            binding.mapLayout.apply {
                                rotation = 270f
                                translationX = 0f
                                translationY =
                                    -(binding.camera.height - this.height).toFloat()
                            }
                        }

                        180 -> {
                            binding.watermarkLayout.apply {
                                rotation = 180f
                                translationX = binding.camera.width.toFloat() - px20.toFloat()
                                translationY =
                                    -(binding.camera.height.toFloat() - px20.toFloat())
                            }
                            binding.northLayout.apply {
                                rotation = 180f
                                translationX = binding.camera.width - CommonUtils.dp2px(
                                    requireContext(),
                                    68 + 20f
                                ).toFloat()
                                translationY = binding.camera.height - CommonUtils.dp2px(
                                    requireContext(),
                                    68 + 20f
                                ).toFloat()
                            }
                            binding.imageWatermark.apply {
                                rotation = 180f
                                translationX =
                                    -(binding.camera.width - 2 * binding.imageWatermark.width - px20).toFloat()
                                translationY = binding.camera.height - px20.toFloat()
                            }
                            binding.mapLayout.apply {
                                rotation = 180f
                                translationX = -(binding.camera.width - this.width).toFloat()
                                translationY =
                                    -(binding.camera.height - this.height).toFloat()
                            }
                        }

                        270 -> {
                            binding.watermarkLayout.apply {
                                rotation = 90f
                                translationX = 0f
                                translationX =
                                    if (binding.watermarkLayout.height + px20 > binding.camera.width
                                    ) {
                                        -(binding.watermarkLayout.height + px20 - binding.camera.width).toFloat()
                                    } else {
                                        0f
                                    }
                                translationY =
                                    -(binding.camera.height.toFloat() - px20.toFloat())
                            }
                            binding.northLayout.apply {
                                rotation = 90f
                                translationX = binding.camera.width - CommonUtils.dp2px(
                                    requireContext(),
                                    68 + 20f
                                ).toFloat()
                                translationY = 0f
                            }
                            binding.imageWatermark.apply {
                                rotation = 90f
                                translationX = binding.imageWatermark.width.toFloat()
                                translationY =
                                    binding.camera.height - binding.imageWatermark.width - px20.toFloat()
                            }
                            binding.mapLayout.apply {
                                rotation = 90f
                                translationX = -(binding.camera.width - this.width).toFloat()
                                translationY = 0f
                            }
                        }

                    }
                }
            })

            mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
            mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS) // Tap to focus!
            mapGesture(Gesture.LONG_TAP, GestureAction.TAKE_PICTURE) // Long tap to shoot!
            mapGesture(
                Gesture.SCROLL_HORIZONTAL,
                GestureAction.EXPOSURE_CORRECTION
            ) // Long tap to shoot!
            mapGesture(
                Gesture.SCROLL_VERTICAL,
                GestureAction.FILTER_CONTROL_1
            ) // Long tap to shoot!
            setOnTouchListener { _, _ ->
                if (binding.flashLayout.isShown) {
                    updateFlashLayout(false)
                }
                false
            }
            if (Global.isVideo) {
                setupVideoSize()
            } else {
                setupPictureSize()
            }

        }
        with(binding.camera.cameraOptions) {
            Logger.e("fps range: ${this?.previewFrameRateMinValue}, ${this?.previewFrameRateMaxValue}")
        }

    }

    private fun setupPictureSize() {

        val width = SizeSelectors.minWidth(targetWidth)
        val height = SizeSelectors.minHeight(targetHeight)
        val dimensions =
            SizeSelectors.and(width, height) // Matches sizes bigger than 1000x2000.
        val ratio = SizeSelectors.aspectRatio(
            AspectRatio.of(targetWidth, targetHeight),
            0f
        ) // Matches 1:1 sizes.
        val result = SizeSelectors.or(
            SizeSelectors.and(ratio, dimensions),  // Try to match both constraints
            ratio,  // If none is found, at least try to match the aspect ratio
            SizeSelectors.biggest() // If none is found, take the biggest
        )
        Logger.e("camera size: ${binding.camera.width}--${binding.camera.height}")
        binding.camera.apply {
            setPictureSize(result)
            setPreviewStreamSize(SizeSelectors.or(ratio, SizeSelectors.biggest()))
            this.layoutParams.height = (this.width.toFloat() * targetHeight / targetWidth).toInt()
            this.layoutParams = layoutParams

            Logger.e("camera size: ${binding.camera.width}--${binding.camera.height}")

//            setPictureSize { list ->
//                Logger.d("picture size")
//                //[1728x4032, 3024x3024, 1440x1920, 1088x1088, 480x640, 288x352, 144x176, 360x640, 3024x4032, 2160x3840, 720x1280, 720x960, 480x720, 240x320, 1816x4032, 1080x2400, 1080x1920, 824x1920, 1080x1440, 2268x4032]
//                Logger.d(list)
//                var tempList = list.filter { it.width.toDouble() / it.height == 3.0 / 4 }
//                //[1440x1920, 480x640, 3024x4032, 720x960, 240x320, 1080x1440]
//                Logger.d(tempList)
//                list
//            }
        }
    }

    private fun setupVideoSize() {
        val width = SizeSelectors.minWidth(videoTargetWidth)
        val height = SizeSelectors.minHeight(videoTargetHeight)
        val dimensions =
            SizeSelectors.and(width, height) // Matches sizes bigger than 1000x2000.
        val ratio = SizeSelectors.aspectRatio(
            AspectRatio.of(videoTargetWidth, videoTargetHeight),
            0f
        ) // Matches 1:1 sizes.
        val result = SizeSelectors.or(
            SizeSelectors.and(ratio, dimensions),  // Try to match both constraints
            ratio,  // If none is found, at least try to match the aspect ratio
            SizeSelectors.biggest() // If none is found, take the biggest
        )
        binding.camera.apply {
            setVideoSize(result)
//            setPreviewStreamSize(object : SizeSelector{
//                override fun select(p0: MutableList<com.otaliastudios.cameraview.size.Size>): MutableList<com.otaliastudios.cameraview.size.Size> {
//                    Logger.e("preview size")
//                    Logger.d(p0)
//                    return  p0
//                }
//            })
//            setPreviewStreamSize(SizeSelectors.or(
//                SizeSelectors.withFilter { it.height == targetHeight && it.width == targetWidth },
//                SizeSelectors.biggest()
//            ))
            setPreviewStreamSize(SizeSelectors.withFilter { it.height == videoTargetHeight && it.width == videoTargetWidth })
            snapshotMaxHeight = videoTargetHeight
            snapshotMaxWidth = videoTargetWidth
            this.layoutParams.height =
                (this.width.toFloat() * videoTargetHeight / videoTargetWidth).toInt()
            this.layoutParams = layoutParams
            Logger.e("${AspectRatio.of(videoTargetWidth, videoTargetHeight).toFloat()}")
            Logger.e("video target size: $videoTargetWidth--$videoTargetHeight")
            Logger.e("camera size: ${binding.camera.width}--${binding.camera.height}")
//            setVideoSize { list ->
//                Logger.d("video size")
//                //[1440x1920, 1088x1088, 480x640, 288x352, 144x176, 360x640, 2160x3840, 720x1280, 720x960, 480x720, 240x320, 1080x2400, 1080x1920, 824x1920, 1080x1440]
//              [1080x1920, 824x1920, 1080x1440, 720x1280, 1088x1088, 720x960, 480x720, 480x640, 360x640, 288x352, 240x320, 144x176]
//                Logger.d(list)
//                var tempList = list.filter { it.width.toDouble() / it.height == 3.0 / 4 }
//                //[1440x1920, 480x640, 720x960, 240x320, 1080x1440]
//                Logger.d(tempList)
//                list
//            }
        }
    }


    override fun onResume() {
        Logger.e("onResume: $this")
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        manager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        super.onResume()
        if (Global.firstPhoto) {
            with(requireContext().localConfig) {
                if (!this.getData(Constants.IS_RATED, false) && !this.getData(
                        Constants.FIRST_PHOTO,
                        false
                    )
                ) {
                    RateDialog(requireContext()).show()
                    this.putData(Constants.FIRST_PHOTO, true)
                }
            }
            Global.firstPhoto = false
        }
//        if (Global.updateLocation) startLocationUpdates()
        PlayBillingHelper(requireActivity().application).apply {
            binding.camera.postDelayed({this.queryPurchases(context)}, 1000)
        }
//        PlayBillingHelper(requireActivity().application).queryPurchases(context)
    }

    override fun onPause() {
        Logger.e("onPause: $this")
        manager.unregisterListener(sensorListener)
        if (isRecording) {
            binding.camera.stopVideo()
        }
//        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    override fun onDestroyView() {
        Logger.e("onDestroyView: $this")
        super.onDestroyView()
        if (::interval.isInitialized) {
            interval.cancel()
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEvent(watermarkChangedEvent: WatermarkChangedEvent) {
        when (watermarkChangedEvent.type) {
            WatermarkChangedEvent.TYPE_CHANGED -> {
                if (context == null) return
                setupWatermark()
            }
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.turn -> binding.camera.toggleFacing()
            R.id.takePicture -> {
                if (Global.isVideo) {
                    if (!Global.localVip && Constants.video_resolution_list.find { it.isChecked }?.isPro == true) {
                        Navigation.findNavController(binding.takePicture)
                            .safeNavigate(R.id.action_camera_to_subscribe)
                    } else {
                        if (isRecording) {
                            binding.camera.stopVideo()
                        } else {
                            binding.camera.takeVideoSnapshot(File.createTempFile("video", ".mp4"))
                        }
                    }
                } else {
                    if (!Global.localVip && Constants.image_resolution_list.find { it.isChecked }?.isPro == true) {
                        Navigation.findNavController(binding.takePicture)
                            .safeNavigate(R.id.action_camera_to_subscribe)
                    } else {
                        binding.camera.takePicture()
                        scaleButton(v)
                        scaleButton(binding.camera)
                    }
                }
            }

            R.id.thumb -> {
//                Navigation.findNavController(v)
//                    .safeNavigate(R.id.action_camera_to_photo, Bundle().apply {
//                        putString(
//                            "uri",
//                            if (tempUri != null) tempUri.toString() else ""
//                        )
//                    })
                val intent = Intent(Intent.ACTION_VIEW)
                intent.type = "image/*"
                startActivity(intent)
            }

            R.id.light -> {
                updateFlashLayout(true)
            }

            R.id.light_auto -> {
                binding.light.setImageResource(R.mipmap.ic_flash_auto)
                binding.camera.flash = Flash.AUTO
//                updateFlashLayout(false)
            }

            R.id.light_on -> {
                binding.light.setImageResource(R.mipmap.ic_flash_on)
                binding.camera.flash = Flash.ON
            }

            R.id.light_off -> {
                binding.light.setImageResource(R.mipmap.ic_flash_off)
                binding.camera.flash = Flash.OFF
            }

            R.id.tag -> {
                Navigation.findNavController(v).safeNavigate(R.id.action_camera_to_tag)
            }

            R.id.captureMode -> {
                switchMode(false)
            }

            R.id.videoMode -> {
                switchMode(true)
            }

            R.id.proMode -> {
                Navigation.findNavController(v).safeNavigate(R.id.action_camera_to_subscribe)
            }

            else -> {
                if (binding.flashLayout.isShown) {
                    updateFlashLayout(false)
                }
            }
        }
        if (v.id != R.id.light && binding.flashLayout.isShown) {
            updateFlashLayout(false)
        }
    }

    private fun updateFlashLayout(show: Boolean) {
        if (context == null) return
        if (show) {
            binding.flashLayout.visibility = VISIBLE
            val animator = ObjectAnimator.ofFloat(
                binding.flashLayout,
                "translationX",
                -binding.flashLayout.width.toFloat(),
                0f
            )
            animator.duration = 200
            animator.start()
            animator.doOnEnd {
                if (!isDetached) {
                    binding.flashLayout.visibility = VISIBLE
                }
            }
            binding.topPanel.visibility = INVISIBLE
        } else {
            binding.flashLayout.visibility = VISIBLE
            val animator = ObjectAnimator.ofFloat(
                binding.flashLayout,
                "translationX",
                0f,
                -binding.flashLayout.width.toFloat()
            )
            animator.duration = 200
            animator.start()
            animator.doOnEnd {
                if (!isDetached) {
                    binding.flashLayout.visibility = GONE
                }
            }
            binding.topPanel.visibility = VISIBLE
        }
    }

    private fun scaleButton(view: View) {
        val animationSet = AnimatorSet()
        animationSet.setDuration(200)
        animationSet.play(ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f, 1f))
            .with(ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f, 1f))
        animationSet.start()
    }

    private val locationPermission = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun checkLocationPermission() {
        if (!XXPermissions.isGranted(requireContext(), locationPermission)) {
            XXPermissions.with(requireContext()).permission(locationPermission)
                .request(object : OnPermissionCallback {
                    @SuppressLint("MissingPermission")
                    override fun onGranted(p0: MutableList<String>, allGranted: Boolean) {
                        getLocation()
//                        startLocationUpdates()
                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        super.onDenied(permissions, doNotAskAgain)
                        if (doNotAskAgain) {
                            XXPermissions.startPermissionActivity(requireContext(), permissions)
                        }
                    }

                })
        } else {
            getLocation()
//            startLocationUpdates()
        }
    }
    private fun startLocationUpdates() {
        if (context == null) return
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Logger.e("startLocationUpdates--$location")
        if (location != null) {
            Global.updateLocation = false
            locationUiTask(location!!)
            return
        }
        val locationRequest = LocationRequest.create()
            .setInterval(10000)
            .setFastestInterval(5000)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    private fun getLocation() {
        if (context == null) return
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
//        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                if (context == null) return@addOnSuccessListener
                // Got last known location. In some rare situations this can be null.
                Logger.e(location.toString())
                location?.let {
//                    if (BuildConfig.DEBUG) {
//                        location.latitude = 39.9049628889
//                        location.longitude = 116.4272689819
//                    }
                    locationUiTask(location)
                }
            }.addOnFailureListener {
                Logger.e("get location fail ${it.localizedMessage}")
                it.printStackTrace()
            }.addOnCompleteListener {
                Logger.e("addOnCompleteListener")
            }

    }
    private fun locationUiTask(location: Location) {
        this.location = location
        Global.updateLocation = false
        updateMap(mMap)
        Logger.e(
            "convert: ${
                Location.convert(
                    location.latitude,
                    Location.FORMAT_DEGREES
                )
            }, ${Location.convert(location.longitude, Location.FORMAT_DEGREES)}"
        )
        Logger.e("location:${location}")

        binding.location.text =
            CommonUtils.getFormatLocationString(location)
        binding.altitude.text =
            "Altitude: ${String.format("%.2f", location.altitude)}"
        getWeather(location.latitude, location.longitude)
        getAddressInfo(location.latitude, location.longitude)
    }

    private fun getAddressInfo(lat: Double, lng: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Geocoder(requireContext()).getFromLocation(
                lat,
                lng,
                5
            ) { addresses ->
                if (context == null) return@getFromLocation
                if (addresses.isEmpty()) return@getFromLocation
                Logger.e(addresses[0].toString())
                runMain {
                    if (isDetached) return@runMain
                    if (addresses[0].featureName == null) {
                        binding.address.text = addresses[0].getAddressLine(0)
                    } else {
                        binding.address.text = addresses[0].featureName
                    }
                }
            }
        } else {
            try {
                scopeNetLife(dispatcher = Dispatchers.IO) {
                    val result = Geocoder(requireContext()).getFromLocation(
                        lat,
                        lng,
                        5
                    )
                    if (context == null) return@scopeNetLife
                    if (!result.isNullOrEmpty()) {
                        runMain {
                            if (isDetached) return@runMain
                            if (result[0].featureName == null) {
                                binding.address.text = result[0].getAddressLine(0)
                            } else {
                                binding.address.text = result[0].featureName
                            }
                        }
                        Logger.e(result[0].toString())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getWeather(lat: Double, lng: Double) {
        val languageCode = Locale.getDefault().language
        scopeNetLife {
            val result =
                Get<String>("http://api.weatherapi.com/v1/current.json?key=d7dc671cb791421aaf930024240107&q=$lat,$lng&lang=$languageCode").await()
            try {
                val obj = JSONObject(result).optJSONObject("current").optJSONObject("condition")
                obj.optString("text")
                obj.optString("icon")
                Logger.e(obj.toString())
                if (context == null) return@scopeNetLife
                binding.weather.text = obj.optString("text")
                Glide.with(binding.weatherImage)
                    .load("https://${obj.optString("icon")}")
                    .into(binding.weatherImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var imageList = mutableListOf<ImageBean>()
    private fun query() {
        scopeNetLife {
            imageList = MediaStoreUtils.query(requireContext())
            if (imageList.isNotEmpty()) {
                tempUri = imageList[0].uri
//                binding.thumb.setImageURI(tempUri)
                loadThumb(imageList[0])
            }

        }
    }

    private fun loadThumb(bean: ImageBean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.thumb.setImageBitmap(
                binding.root.context.contentResolver.loadThumbnail(
                    bean.uri,
                    Size(300, 400),
                    null
                )
            )
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                binding.root.context.contentResolver,
                bean.id,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null
            )
        }
    }

    fun updateNorthLayout(degree: Float) {
        if (context == null) return
        binding.northBg.rotation = curOrientation - degree
    }

    private var sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            /**
             * values[0]: x-axis 方向加速度
             * 　　 values[1]: y-axis 方向加速度
             * 　　 values[2]: z-axis 方向加速度
             */
            val degree = event.values[0] // 存放了方向值
//            Logger.e("degree:$degree")
            updateNorthLayout(degree)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        }

    }

    private var location: Location? = null
    var mMap: GoogleMap? = null
    var isMapLoaded = false
    private fun addMap() {
        val mapFragment = SupportMapFragment.newInstance()
        requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.mapLayout, mapFragment)
            .commit()
        mapFragment.getMapAsync { map ->
            mMap = map
            updateMap(map)
            map.setOnMapLoadedCallback {
                isMapLoaded = true
                Logger.e("setOnMapLoadedCallback: true")
                updateMapImage()
            }

        }
    }

    private fun updateMap(map: GoogleMap?) {
        map?.let {
            Logger.e("onMapReady")
            location?.let {
                map.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        15f
                    )
                )
                updateMapImage()
            }
        }
    }

    private fun updateMapImage() {
        if (!Global.map) return
        if (isMapLoaded && location != null && mMap != null) {
            Logger.e("updateMapImage")
            scopeNetLife {
                location?.let {
                    if (isResumed) {
                        mMap!!.snapshot {
                            Logger.e("updateMapImage--snapshot")
                            binding.mapImage.setImageBitmap(it)
                            binding.mapImage.visibility = VISIBLE
                            binding.mapLayout.getChildAt(1)?.visibility = GONE
                        }
                    }
                }
            }
        }
    }


    var targetWidth = 1920
    var targetHeight = 2560

    var videoTargetWidth = 1080
    var videoTargetHeight = 1920
    private fun initPictureSpinner() {
        var index =
            Constants.image_resolution_list.indexOfFirst { it.width == targetWidth && it.height == targetHeight }
        if (index == -1) {
            index = 0
        }
        Constants.image_resolution_list[index].isChecked = true
        binding.pictureSizeSpinner.dropDownVerticalOffset = CommonUtils.dp2px(requireContext(), 10f)
        binding.pictureSizeSpinner.adapter =
            ResolutionAdapter(Constants.image_resolution_list).apply {
                setListener(object : ResolutionAdapter.OnItemClickListener {
                    override fun onItemClicked(position: Int) {
                        hideSpinnerDropDown(binding.pictureSizeSpinner)
                        if (!Global.localVip && Constants.image_resolution_list[position].isPro) {
                            Navigation.findNavController(binding.pictureSizeSpinner)
                                .safeNavigate(R.id.action_camera_to_subscribe)
                            return
                        }
                        binding.pictureSizeSpinner.setSelection(position)
                    }
                })
            }
        binding.pictureSizeSpinner.setSelection(index)
        binding.pictureSizeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Logger.e("onItemSelected")
                Constants.image_resolution_list.find { it.isChecked }?.let {
                    it.isChecked = false
                }
                with(Constants.image_resolution_list[position]) {
                    targetWidth = this.width
                    targetHeight = this.height
                    this.isChecked = true
                    setupPictureSize()
                }
                (binding.pictureSizeSpinner.adapter as ResolutionAdapter).notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    private fun initVideoSpinner() {
        var index =
            Constants.video_resolution_list.indexOfFirst { it.width == videoTargetWidth && it.height == videoTargetHeight }
        if (index == -1) {
            index = 0
        }
        Constants.video_resolution_list[index].isChecked = true
        binding.videoSizeSpinner.dropDownVerticalOffset = CommonUtils.dp2px(requireContext(), 10f)
        binding.videoSizeSpinner.adapter =
            ResolutionAdapter(Constants.video_resolution_list).apply {
                setListener(object : ResolutionAdapter.OnItemClickListener {
                    override fun onItemClicked(position: Int) {
                        hideSpinnerDropDown(binding.videoSizeSpinner)
                        if (!Global.localVip && Constants.video_resolution_list[position].isPro) {
                            Navigation.findNavController(binding.videoSizeSpinner)
                                .safeNavigate(R.id.action_camera_to_subscribe)
                            return
                        }
                        binding.videoSizeSpinner.setSelection(position)
                    }
                })
            }
        binding.videoSizeSpinner.setSelection(index)
        binding.videoSizeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Logger.e("onItemSelected")
                Constants.video_resolution_list.find { it.isChecked }?.let {
                    it.isChecked = false
                }
                with(Constants.video_resolution_list[position]) {
                    videoTargetWidth = this.width
                    videoTargetHeight = this.height
                    this.isChecked = true
                    setupVideoSize()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }


    var isRecording = false
    private fun switchMode(videoMode: Boolean) {
        if (context == null) return
        isVideo = videoMode
        binding.captureMode.alpha = if (isVideo) 0.5f else 1f
        binding.videoMode.alpha = if (isVideo) 1f else 0.5f
        binding.pictureSizeSpinner.visibility = if (isVideo) GONE else VISIBLE
        binding.videoSizeSpinner.visibility = if (isVideo) VISIBLE else GONE
        binding.videoDot.visibility = if (isVideo) VISIBLE else GONE
        binding.photoDot.visibility = if (isVideo) GONE else VISIBLE

        binding.camera.mode = if (isVideo) Mode.VIDEO else Mode.PICTURE

        binding.takePicture.setImageResource(if (isVideo) R.mipmap.ic_take_video else R.mipmap.ic_take_photo)

        if (isVideo) setupVideoSize() else setupPictureSize()
        updateBottomView()
    }

    fun hideSpinnerDropDown(spinner: Spinner?) {
        try {
            val method: Method = Spinner::class.java.getDeclaredMethod("onDetachedFromWindow")
            method.isAccessible = true
            method.invoke(spinner)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}