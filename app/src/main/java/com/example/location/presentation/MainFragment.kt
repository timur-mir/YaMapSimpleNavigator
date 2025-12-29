package com.example.location.presentation

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.widget.MediaController
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.location.presentation.ApplicationMapKit.LocalHelp.loc
import com.example.location.presentation.ApplicationMapKit.LocalHelp.locMark
import com.example.location.presentation.ApplicationMapKit.LocalHelp.markAdd
import com.example.location.presentation.ApplicationMapKit.LocalHelp.marksSize
import com.example.location.presentation.ApplicationMapKit.LocalHelp.myLocation
import com.example.location.presentation.ApplicationMapKit.LocalHelp.offOn
import com.example.location.databinding.MainFragmentBinding
import com.example.location.domain.Mark
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.NotFoundError
import com.yandex.runtime.network.RemoteError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.location.R
import com.example.location.presentation.ApplicationMapKit.LocalHelp.activityClose
import com.example.location.presentation.ApplicationMapKit.LocalHelp.lastPoint
import com.example.location.presentation.ApplicationMapKit.LocalHelp.routeProcess
import com.example.location.data.roomrepo.getScaledBitmap
import com.example.location.presentation.ApplicationMapKit.LocalHelp.actualLoc
import com.example.location.presentation.ApplicationMapKit.LocalHelp.actualLocationFlag
import com.example.location.presentation.ApplicationMapKit.LocalHelp.currentArea
import com.example.location.presentation.ApplicationMapKit.LocalHelp.offOnUserLayer
import com.example.location.presentation.ApplicationMapKit.LocalHelp.weatherRequest
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val REQUEST_PHOTO = 2

class MainFragment : Fragment(), com.yandex.mapkit.search.Session.SearchListener, CameraListener,
    UserLocationObjectListener, Transaction, DrivingSession.DrivingRouteListener,
    SensorEventListener {
    lateinit var placemarkTapListener: MapObjectTapListener
    private val filesDir = ApplicationMapKit.applicationContext().filesDir
    private lateinit var photoFile: File
    private var photoUri: Uri? = null
    private var _binding: MainFragmentBinding? = null
    val binding get() = _binding!!
    lateinit var searchSession: com.yandex.mapkit.search.Session
    lateinit var searchManager: SearchManager
    lateinit var locationManager: LocationManager
    lateinit var splitInstallManager: SplitInstallManager
    lateinit var locationmapkit: UserLocationLayer
    lateinit var panoramaPlaceFragment: PanoramaPlaceFragment
    lateinit var showAllMarksFragment: ShowAllMarksFragment
    lateinit var geocoder: Geocoder

    var lat: Double = 0.0
    var lon: Double = 0.0
    var azimuth: Float = 0.0f
    var tilt: Float = 0.0f
    var zoom: Float = 0.0f
    lateinit var toast: Toast
    private val RECOGNIZER_RESULT = 1234
    lateinit var startLocationPoints: Point
    lateinit var endLocationPoints: Point
    lateinit var midleLocationPoints: Point
    private var mapObjectsMain: MapObjectCollection? = null
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null
    var endLocationPointsEl: MutableList<android.location.Address>? = null
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var compasSensor: Sensor? = null
    private var compasSensor2: Sensor? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    var azimuthCompas: Float = 0.0f
    private val marksViewModel by viewModels<MarksViewModel>
    {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MarksViewModel() as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater)
        return binding.root
    }

    private fun jumpToNextActivity() {
        val intent = Intent(requireContext(), StepActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        activity?.finish()
    }

    private fun registerSensor() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Toast.makeText(requireContext(), "Шагомер включён!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Сенсор движения не найден!", Toast.LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.goNext.setOnClickListener {
            jumpToNextActivity()
        }
        placemarkTapListener = MapObjectTapListener { obj, point ->
            tapListener(point, obj)
            true
        }
        geocoder = Geocoder(requireActivity())
        endLocationPoints = Point(55.751574, 37.573856)
        val mapKit: MapKit = MapKitFactory.getInstance()
        val probki = mapKit.createTrafficLayer(binding.mapview.mapWindow)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        locationmapkit = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjectsMain = binding.mapview.map.mapObjects.addCollection()
        binding.mapview.map.isRotateGesturesEnabled = true
        binding.userroute.setOnClickListener {
            routeProcess = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text")
            startActivityForResult(intent, RECOGNIZER_RESULT)
        }

        binding.userlocation!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (requireActivity().checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                        100
                    )
                }
            }
            if (ApplicationMapKit.LocalHelp.offOnUserLayer) {
                if (::locationmapkit.isInitialized) {
                    locationmapkit!!.isVisible = true
                    locationmapkit!!.setObjectListener(this)
                    ApplicationMapKit.LocalHelp.offOnUserLayer = false
                    running = true
                    binding.stepsInfo.text = "Начинайте движение →"
                    registerSensor()
                    loadData()
                    resetSteps()
                }
            } else {
                running = false
                locationmapkit.isVisible = false
                locationmapkit.setObjectListener(null)
                ApplicationMapKit.LocalHelp.offOnUserLayer = true
                if (::sensorManager.isInitialized) {
                    sensorManager.unregisterListener(this)
                }
                binding.stepsInfo.text = "Включите режим наблюдения перемещения ↑"
            }
        }
        //Для добавления кода при повороте
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (locMark != null) {

                    if (markAdd == true) {
                        binding.mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(requireContext(), R.drawable.user_mark)
                        ).addTapListener(placemarkTapListener)

                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if (locMark != null) {
                    if (markAdd == true) {
                        binding.mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(requireContext(), R.drawable.user_mark)
                        ).addTapListener(placemarkTapListener)
                    }
                }
            }

            else -> {
            }
        }
        Toast.makeText(
            requireContext(),
            "Загрузка карт...Определяется местоположение по местности...",
            Toast.LENGTH_LONG
        ).show()
        binding.zoombtn.setOnClickListener {
            myLocation?.let { it1 -> zoom(0, it1) }
        }
        binding.zoombtndec.setOnClickListener {
            myLocation?.let { it1 -> zoom(1, it1) }
        }
        binding.sendLocation.setOnClickListener {
            if (loc == null) {
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${loc!!.position.latitude},${loc!!.position.longitude}"
                    )
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.locationCoordinates)
                    )
                }.also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.sendingLocationCoordinates))
                    startActivity(chooserIntent)
                }
            }
        }
        binding.showAllLocationMark.setOnClickListener {
            showAllMarksFragment = ShowAllMarksFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, showAllMarksFragment).commit()
            requireActivity().findViewById<FrameLayout>(R.id.navHostFragment).bringToFront()
        }
        binding.locationCurrentAddMarker.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                marksViewModel.getMarksSize()
            }
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                delay(200)
                marksViewModel.marksSize.collect { mSize ->
                    marksSize = mSize
                }
            }
            loc?.let { location ->
                val mapObjects = binding.mapview.map.mapObjects
                locMark = location
                val snackbar = Snackbar.make(
                    binding.root,
                    "Маркер на карту",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.addMark)) {
                        AlertDialog.Builder(requireActivity())
                            .setCancelable(false)
                            .setPositiveButton("Добавить") { _, _ ->
                                run {
                                    val packageManager: PackageManager =
                                        requireActivity().packageManager
                                    val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    val resolvedActivity: ResolveInfo? =
                                        packageManager.resolveActivity(
                                            captureImage,
                                            PackageManager.MATCH_DEFAULT_ONLY
                                        )
                                    if (resolvedActivity != null) {
                                        photoFile =
                                            File(filesDir, "PhotoPlace_${marksSize + 1}.jpg")
                                        photoUri = FileProvider.getUriForFile(
                                            requireActivity(),
                                            "com.example.location.fileprovider",
                                            photoFile
                                        )
                                        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                        val cameraActivities: List<ResolveInfo> =
                                            packageManager.queryIntentActivities(
                                                captureImage,
                                                PackageManager.MATCH_DEFAULT_ONLY
                                            )
                                        for (cameraActivity in cameraActivities) {
                                            requireActivity().grantUriPermission(
                                                cameraActivity.activityInfo.packageName,
                                                photoUri,
                                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                            )
                                        }
                                        startActivityForResult(captureImage, REQUEST_PHOTO)
                                    }
                                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                        delay(350)
                                        marksViewModel.addMark(
                                            Mark(
                                                marksSize + 1,
                                                locMark!!.position.longitude,
                                                locMark!!.position.latitude,
                                                photoFileName = "PhotoPlace_${marksSize + 1}.jpg"
                                            )
                                        )
                                    }
                                    mapObjects.addPlacemark(
                                        locMark!!.position,
                                        ImageProvider.fromResource(
                                            requireContext(),
                                            R.drawable.us_m2
                                        )
                                    ).apply { userData = "PhotoPlace_${marksSize + 1}.jpg" }
                                }
                            }
                            .setNegativeButton("Отменить", null)
                            .setTitle("Добавление маркера и фото местности")
                            .show()

                    }
                snackbar.setActionTextColor(Color.WHITE)
                snackbar.setBackgroundTint((Color.BLUE))
                    .show()
            }
        }
        binding.prob.setOnClickListener {
            if (offOn == false) {
                probki.isTrafficVisible = true
                offOn = true
            } else {
                probki.isTrafficVisible = false
                offOn = false
            }

        }
        binding.geoPositionBtn.setOnClickListener {
            getLocation()
            toast =
                Toast.makeText(
                    requireContext(),
                    getString(R.string.yourCoordinates) + ": ${loc!!.position.latitude} и ${loc!!.position.longitude}" +
                            " _ ${currentArea}",
                    Toast.LENGTH_LONG
                )
            toast.show()

        }
        SearchFactory.initialize(requireContext())
        searchManager =
            SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        binding.mapview.map.addCameraListener(this)

        binding.voicesearch.setOnClickListener {
            routeProcess = false
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text")
            startActivityForResult(intent, RECOGNIZER_RESULT)
        }
        binding.searchField.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                queryPlace(binding.searchField.text.toString())
            }
            false
        }
        savedInstanceState?.let { bundle ->
            lon = bundle.getDouble(KEY_LON)
            lat = bundle.getDouble(KEY_LAT)
            zoom = bundle.getFloat(KEY_ZOOM)
            azimuth = bundle.getFloat(KEY_AZIMUTH)
            tilt = bundle.getFloat(KEY_TILT)
            setLocationAfterRotate(lat, lon, zoom, azimuth, tilt)

        } ?: setLocation()

    }

    @RequiresApi(Build.VERSION_CODES.P)

    private fun tapListener(point: Point, objectMap: MapObject) {
        val li = LayoutInflater.from(requireActivity())
        val mark_info_view: View = li.inflate(R.layout.info_about_mark, null)
        val text = mark_info_view.findViewById<View>(R.id.main_info) as TextView
        val image = mark_info_view.findViewById<View>(R.id.mark_pict) as ImageView
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            marksViewModel.getAllMarks()
            marksViewModel.marks2.collect { marks ->
                var i = 0
                if (marks?.size != 0 && marks != null) {
                    while (i < marks.size) {
                        requireActivity().runOnUiThread {
                            image.setImageBitmap(getBitmapPlaceMark(marks[i].photoFileName))
                            text.text = "Фото места: ${marks[i].photoFileName.toString()}"
                            image.setOnClickListener {
                                val photoFileFragment = PhotoFileFragment()
                                val args: Bundle = Bundle()
                                val pathPhoto = getPathPhoto(marks[i].photoFileName)
                                val photoName = marks[i].photoFileName
                                args.putString("path", pathPhoto);
                                args.putString("photo", photoName)
                                photoFileFragment.arguments = args;
                                photoFileFragment.show(childFragmentManager, "photoMark")
                            }
                        }
                        delay(3000)
                        i += 1
                        if (i == marks.size) {
                            i -= 1
                            break
                        }
                    }

                }
            }

        }

        requireActivity().runOnUiThread {
            AlertDialog.Builder(requireActivity())
                .setView(mark_info_view)
                .setCancelable(true)
                .setPositiveButton("Закрыть", null)
                .show()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            val matches: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ApplicationMapKit.LocalHelp.speachText = matches?.get(0)?.toString() ?: "Деловой центр"
            if (routeProcess) {
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    endLocationPointsEl =
                        matches?.get(0)?.toString()?.let { geocoder.getFromLocationName(it, 2) }
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1200)
                    if (endLocationPointsEl != null && endLocationPointsEl!!.isNotEmpty()) {
                        endLocationPoints =
                            Point(
                                endLocationPointsEl!![0].latitude,
                                endLocationPointsEl!![0].longitude
                            )
                        delay(1500)
                        startRoute()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            " Попробуйте ещё раз",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                matches?.get(0)?.toString()
                    ?.let { queryPlace(matches?.get(0)?.toString() ?: "Театральная") }
            }

        } else if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            val bitmap =
                getScaledBitmap(photoFile.path, requireActivity())
            val fos = FileOutputStream(photoFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
            // Handler().postDelayed({ getLocation() }, 200)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        val mapObjects = binding.mapview.map.mapObjects
        Handler().postDelayed({
            if (loc == null || routeProcess) {
                getLocation()
            }
        }, 2000)
        Handler().postDelayed({
            if (loc != null) {
                activity?.runOnUiThread {
                    binding.userroute.isEnabled = true
                    binding.voicesearch.isEnabled = true
                    binding.zoombtn.isEnabled = true
                    binding.zoombtndec.isEnabled = true
                    binding.locationCurrentAddMarker.isEnabled = true
                    binding.sendLocation.isEnabled = true
                    binding.searchField.isEnabled = true
                    binding.prob.isEnabled = true
                    binding.userlocation.isEnabled = true
                    binding.showAllLocationMark.isEnabled = true
                }
            }
        }, 2400)
        viewLifecycleOwner.lifecycleScope.launch {
            marksViewModel.marks.collect { marks ->
                if (marks != null) {
                    marksSize = marks.size
                    marks.forEach {
                        mapObjects.addPlacemark(
                            Point(it.coordinateLat, it.coordinateLong),
                            ImageProvider.fromResource(requireContext(), R.drawable.user_mark)
                        ).addTapListener(placemarkTapListener)

                    }

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getBitmapPlaceMark(fileName: String): Bitmap {
        photoFile = File(filesDir, fileName)
        val source = ImageDecoder.createSource(
            photoFile
        )
        return ImageDecoder.decodeBitmap(source)
    }

    private fun getPathPhoto(fileName: String): String {
        photoFile = File(filesDir, fileName)
        return photoFile.path
    }

    private fun inputListenerOnMap(): InputListener {
        val inputListener: InputListener = object : InputListener {
            override fun onMapTap(p0: Map, p1: Point) {
                val request = SplitInstallRequest.newBuilder()
                    .addModule("panoramafeature")
                    .build()
                splitInstallManager = SplitInstallManagerFactory.create(context);
                splitInstallManager.startInstall(request)
                    .addOnSuccessListener {
                        activityClose = true
                        val intent = Intent().setClassName(
                            requireContext(),
                            "home.howework.panoramafeature.PanoramaActivityF"
                        )
                        intent.putExtra("lat", p1.latitude)
                        intent.putExtra("long", p1.longitude)
                        intent.flags =
                            (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        activity?.finish()
                    }

            }

            override fun onMapLongTap(p0: com.yandex.mapkit.map.Map, p1: Point) {
            }
        }
        return inputListener
    }

    override fun onStop() {
        binding.mapview.onStop()
        routeProcess = false
//        actualLocationFlag = false
        locationManager.unsubscribe(localListener())
        offOnUserLayer = true
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        if (currentArea.isNotEmpty()) {
            binding.localInfo.text = currentArea
        }
        binding.mapview.map.addInputListener(inputListenerOnMap())
        binding.mapview.map.isRotateGesturesEnabled = true
        binding.mapview.map.isScrollGesturesEnabled = true
        binding.mapview.onStart()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        if (binding.mapview.map.isValid) {
            bundle.putDouble(KEY_LAT, binding.mapview.map.cameraPosition.target.latitude)
            bundle.putDouble(KEY_LON, binding.mapview.map.cameraPosition.target.longitude)
            bundle.putFloat(KEY_AZIMUTH, binding.mapview.map.cameraPosition.azimuth)
            bundle.putFloat(KEY_TILT, binding.mapview.map.cameraPosition.tilt)
            bundle.putFloat(KEY_ZOOM, binding.mapview.map.cameraPosition.zoom)
            super.onSaveInstanceState(bundle)
        }
    }


    private fun setLocation() {
        binding.mapview.map.move(
            CameraPosition(Point(55.751574, 37.573856), 13.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f), null
        )
    }

    private fun setLocationAfterRotate(
        lat: Double,
        lon: Double,
        zoom: Float,
        azimuth: Float,
        tilt: Float
    ) {
        binding.mapview.map.move(
            CameraPosition(Point(lat, lon), zoom, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, 1f), null
        )
    }


    private fun zoom(sender: Int, point: Point) {
        val zoomStep: Float = if (sender == 0) -1f else 1f
        val position =
            CameraPosition(point, binding.mapview.map.cameraPosition.zoom + zoomStep, 0F, 0F)
        binding.mapview.map.move(
            position, Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    private fun queryPlace(query: String) {
        searchSession = searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(binding.mapview.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    fun getWeatherInLocationPlace(latitude: String, longitude: String, town: String) {
        Thread() {
            try {
                val allInfo = URL(
                    "https://api.open-meteo.com/v1/forecast?latitude=$latitude" +
                            "&longitude=$longitude&hourly=temperature_2m,weathercode"
                ).readText(Charsets.UTF_8)
                if (allInfo.isNotEmpty()) {
                    val hourly = JSONObject(allInfo).getJSONObject("hourly")
                    val tempDayHalf = hourly.getJSONArray("temperature_2m").getDouble(13).toInt()
//                    if (!actualLocationFlag) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireActivity(),
                                "Температура сегодня в полдень :$tempDayHalf°С",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("ResponseL", tempDayHalf.toString())
                        }
//                    }

                } else {
                    Thread.currentThread().interrupt()
                }
            } catch (e: InterruptedException) {
                Log.e("RemoteError", e.message.toString())
            }

        }.start()
    }

    private fun localListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationUpdated(location: Location) {
//                if (!actualLocationFlag) {
//                    loc = location
//                    actualLoc = location
//                    actualLocationFlag = true
//                } else {
//                    loc = location
//                }
                loc = location

                val town = geocoder.getFromLocation(
                    location.position.latitude,
                    location.position.longitude,
                    1
                )

                binding.localInfo.text = town!![0].adminArea.toString()
                currentArea = town!![0].adminArea.toString()


                if (isAdded) {
                    //......//
                }
                if(weatherRequest) {
                    getWeatherInLocationPlace(
                        location.position.latitude.toString(),
                        location.position.longitude.toString(),
                        town!![0].adminArea.toString()
                    )
                    panoramaPlaceFragment = PanoramaPlaceFragment.newInstance(
                        location.position.latitude,
                        location.position.longitude
                    )
                    weatherRequest=false
                    (activity as Transaction).navigateTo(panoramaPlaceFragment)
                }

                myLocation = location.position
                if (myLocation != null) {
                    ApplicationMapKit.LocalHelp.latitudeActivity = location.position.latitude
                    ApplicationMapKit.LocalHelp.longitudeActivity = location.position.longitude
                    turnButtons()
                }
            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::toast.isInitialized) {
            toast.cancel()
        }
        _binding = null

    }

    private fun turnButtons() {
        if (loc != null) {
            binding.userroute.isEnabled = true
            binding.voicesearch.isEnabled = true
            binding.zoombtn.isEnabled = true
            binding.zoombtndec.isEnabled = true
            binding.locationCurrentAddMarker.isEnabled = true
            binding.sendLocation.isEnabled = true
            binding.searchField.isEnabled = true
            binding.prob.isEnabled = true
            binding.userlocation!!.isEnabled = true
            binding.showAllLocationMark.isEnabled = true
            ApplicationMapKit.LocalHelp.offOnUserLayer = true
        }
    }

    private fun getLocation() {
        weatherRequest=true
        locationManager.subscribeForLocationUpdates(0.0,0,0.0,true,FilteringMode.ON, localListener())
//        locationManager.requestSingleUpdate(
//            localListener()
//        )
    }


    override fun onSearchResponse(response: Response) {
        var needCoordinatesPointer = 0
        if (binding.searchField.text.isNotEmpty() || ApplicationMapKit.LocalHelp.speachText.isNotEmpty()) {
            if (binding.searchField.text.isNotEmpty()) {
                binding.searchField.text.clear()
            }
            ApplicationMapKit.LocalHelp.speachText = ""
            val mapObjects = binding.mapview.map.mapObjects
            mapObjects.clear()
            if (response.collection.children.size > 1 && response.collection.children.size % 2 == 0) {
                needCoordinatesPointer = 0
                val snackbar = Snackbar.make(
                    binding.root,
                    "Результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.WHITE)
                snackbar.setBackgroundTint((Color.RED))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(
                        Point(
                            response.collection.children[0].obj!!.geometry[0].point!!.latitude,
                            response.collection.children[0].obj!!.geometry[0].point!!.longitude
                        ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt
                    ),
                    Animation(Animation.Type.SMOOTH, 1.0f), null
                )

                val menuItem =
                    requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(
                        0
                    )
                menuItem.title = resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main)
                    .setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity =
                    response.collection.children[needCoordinatesPointer].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity =
                    response.collection.children[needCoordinatesPointer].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    if (response.collection.children.size > 0) {
                        panoramaPlaceFragment = PanoramaPlaceFragment.newInstance(
                            ApplicationMapKit.LocalHelp.latitudeActivity,
                            ApplicationMapKit.LocalHelp.longitudeActivity
                        )
                        (activity as Transaction).navigateTo(panoramaPlaceFragment)
                    }
                }
            } else if (response.collection.children.size == 1) {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.WHITE)
                snackbar.setBackgroundTint((Color.RED))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(
                        Point(
                            response.collection.children[0].obj!!.geometry[0].point!!.latitude,
                            response.collection.children[0].obj!!.geometry[0].point!!.longitude
                        ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt
                    ),
                    Animation(Animation.Type.SMOOTH, 1.0f), null
                )

                val menuItem =
                    requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(
                        0
                    )
                menuItem.title = resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main)
                    .setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity =
                    response.collection.children[0].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity =
                    response.collection.children[0].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    if (response.collection.children.size > 0) {
                        panoramaPlaceFragment = PanoramaPlaceFragment.newInstance(
                            ApplicationMapKit.LocalHelp.latitudeActivity,
                            ApplicationMapKit.LocalHelp.longitudeActivity
                        )
                        (activity as Transaction).navigateTo(panoramaPlaceFragment)
                    }
                }
            } else if (response.collection.children.size > 2) {
                val snackbar = Snackbar.make(
                    binding.root,
                    "Результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.WHITE)
                snackbar.setBackgroundTint((Color.RED))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(
                        Point(
                            response.collection.children[0].obj!!.geometry[0].point!!.latitude,
                            response.collection.children[0].obj!!.geometry[0].point!!.longitude
                        ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt
                    ),
                    Animation(Animation.Type.SMOOTH, 1.0f), null
                )

                val menuItem =
                    requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(
                        0
                    )
                menuItem.title = resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main)
                    .setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity =
                    response.collection.children[0].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity =
                    response.collection.children[0].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    if (response.collection.children.size > 0) {
                        panoramaPlaceFragment = PanoramaPlaceFragment.newInstance(
                            ApplicationMapKit.LocalHelp.latitudeActivity,
                            ApplicationMapKit.LocalHelp.longitudeActivity
                        )
                        (activity as Transaction).navigateTo(panoramaPlaceFragment)
                    }
                }

            }
            for (searchResult in response.collection.children) {
                val resultLocation = searchResult.obj!!.geometry[0].point!!
                if (response != null) {
                    mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(requireContext(), R.drawable.search_result)
                    )
                }
            }
        }
    }

    override fun onSearchError(error: Error) {
        var errorMessage = "";
        if (error is NotFoundError) {
            errorMessage = getString(R.string.notFoundError)
        } else if (error is RemoteError) {
            errorMessage = getString(R.string.remoteError)
        } else if (error is NetworkError) {
            errorMessage = getString(R.string.networkError)
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {

        }
    }

    companion object {
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_AZIMUTH = "azimuth"
        private const val KEY_TILT = "tilt"
        private const val KEY_ZOOM = "zoom"
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        locationmapkit?.setAnchor(
            PointF(
                (binding.mapview.width() * 0.5).toFloat(),
                binding.mapview.height() * 0.5.toFloat()
            ),
            PointF(
                (binding.mapview.width() * 0.5).toFloat(), binding.mapview.height() * 0.83.toFloat()
            )
        )
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon(

            "icon", ImageProvider.fromResource(requireContext(), R.drawable.compas),
            IconStyle().setAnchor(PointF(0.5f, 05f)).setRotationType(RotationType.ROTATE)
                .setZIndex(1f).setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.GREEN and -0x66000001
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(layer: UserLocationView, p1: ObjectEvent) {

        var direct = layer.arrow.direction
        if (::locationmapkit.isInitialized &&
            locationmapkit.isVisible == true
        ) {
            Handler().postDelayed({
                binding.mapview.map.set2DMode(true)
                binding.mapview.map.move(
                    CameraPosition(
                        Point(
                            locationmapkit.cameraPosition()!!.target.latitude,
                            locationmapkit.cameraPosition()!!.target.longitude
                        ),
                        locationmapkit.cameraPosition()!!.zoom,
                        direct,
                        locationmapkit.cameraPosition()!!.tilt
                    ),
                    Animation(Animation.Type.SMOOTH, 1f), null
                )

            }, 1000)
            //   binding.mapview.rotation=direct
        }


    }

    override fun navigateTo(fragment: Fragment) {
        TODO("Not yet implemented")
    }

    override fun onDrivingRoutes(pointsRoute: MutableList<DrivingRoute>) {
        for (routeCoord in pointsRoute) {
            binding.mapview.map.mapObjects.addPolyline(routeCoord.geometry)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
        Toast.makeText(requireContext(), getString(R.string.notFoundError), Toast.LENGTH_SHORT)
            .show()
    }


    private fun startRoute() {
        binding.mapview.map.mapObjects.clear()
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestRoutePoints: ArrayList<RequestPoint> = ArrayList()
        requestRoutePoints.add(RequestPoint(myLocation!!, RequestPointType.WAYPOINT, null))
        requestRoutePoints.add(RequestPoint(endLocationPoints, RequestPointType.WAYPOINT, null))
        drivingSession =
            drivingRouter!!.requestRoutes(requestRoutePoints, drivingOptions, vehicleOptions, this)
    }

    private fun resetSteps() {
        previousTotalSteps = totalSteps
        binding.stepsInfo.text = "Вы прошли ◄↕► $0 шаг..."
        saveData()

    }

    private fun saveData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        previousTotalSteps = sharedPreferences.getFloat("key1", 0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return
        if (running) {
            totalSteps = event.values[0]
            val currentSteps = (totalSteps - previousTotalSteps).toInt()
            binding.stepsInfo.text = "Вы прошли ◄↕► $currentSteps шаг..."
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}




