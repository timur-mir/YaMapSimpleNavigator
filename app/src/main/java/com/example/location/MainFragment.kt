package com.example.location

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.location.ApplicationMapKit.LocalHelp.loc
import com.example.location.ApplicationMapKit.LocalHelp.locMark
import com.example.location.ApplicationMapKit.LocalHelp.markAdd
import com.example.location.ApplicationMapKit.LocalHelp.myLocation
import com.example.location.ApplicationMapKit.LocalHelp.offOn
import com.example.location.databinding.MainFragmentBinding
import com.example.location.domain.Mark
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

class MainFragment : Fragment(), com.yandex.mapkit.search.Session.SearchListener, CameraListener,
    UserLocationObjectListener {

    private var _binding: MainFragmentBinding? = null
    val binding get() = _binding!!
    lateinit var searchSession: com.yandex.mapkit.search.Session
    lateinit var searchManager: SearchManager
    lateinit var locationManager: LocationManager
    lateinit var splitInstallManager: SplitInstallManager
    lateinit var locationmapkit: UserLocationLayer
    var lat: Double = 0.0
    var lon: Double = 0.0
    var azimuth: Float = 0.0f
    var tilt: Float = 0.0f
    var zoom: Float = 0.0f
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapKit: MapKit = MapKitFactory.getInstance()
        val probki = mapKit.createTrafficLayer(binding.mapview.mapWindow)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        getLocation()
        binding.userlocation!!.setOnClickListener {
            if (ApplicationMapKit.LocalHelp.offOnUserLayer) {
                if (::locationmapkit.isInitialized) {
                    locationmapkit!!.isVisible = true
                    locationmapkit!!.setObjectListener(this)
                    ApplicationMapKit.LocalHelp.offOnUserLayer = false
                } else {
                    locationmapkit = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
                    locationmapkit!!.isVisible = true
                    locationmapkit!!.setObjectListener(this)
                    ApplicationMapKit.LocalHelp.offOnUserLayer = false

                }
            } else {
                locationmapkit.isVisible = false
                locationmapkit.setObjectListener(null)
                ApplicationMapKit.LocalHelp.offOnUserLayer = true
            }

        }
        //Для добавления кода при повороте
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (locMark != null) {

                    if (markAdd == true) {
                        binding.mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(requireContext(), R.drawable.us_m2)
                        )
                        binding.locationCurrentAddMarker.setImageResource(R.drawable.delete)
                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if (locMark != null) {

                    if (markAdd == true) {
                        binding.mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(requireContext(), R.drawable.us_m2)
                        )
                        binding.locationCurrentAddMarker.setImageResource(R.drawable.delete)
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
                getLocation()
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

        binding.delallmarks.setOnClickListener {
            val mapObjects = binding.mapview.map.mapObjects
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                marksViewModel.deleteMarks()
            }
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    mapObjects.clear()

                }

            viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                marksViewModel.marks.collect { marks ->
                    if (marks != null) {
                        marks.forEach {
                            mapObjects.addPlacemark(
                                Point(it.coordinateLat, it.coordinateLong),
                                ImageProvider.fromResource(
                                    requireContext(),
                                    R.drawable.us_m2
                                )
                            )
                        }

                    }
                }
            }


        }
        binding.locationCurrentAddMarker.setOnClickListener {
            if (loc == null) {
                getLocation()
            }
            loc?.let { location ->
                val mapObjects = binding.mapview.map.mapObjects


                if (!markAdd) {
                    locMark = location
                    markAdd = true
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Маркер на карту",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.addMark)) {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                marksViewModel.addMark(
                                    Mark(
                                        0,
                                        locMark!!.position.longitude,
                                        locMark!!.position.latitude
                                    )
                                )
                            }
                            mapObjects.addPlacemark(
                                locMark!!.position,
                                ImageProvider.fromResource(requireContext(), R.drawable.us_m22)
                            )
                            binding.locationCurrentAddMarker.setImageResource(R.drawable.delete)
                        }
                    snackbar.setActionTextColor(Color.WHITE)
                    snackbar.setBackgroundTint((Color.BLUE))

                        .show()
                } else if (markAdd) {
                    markAdd = false
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Убрать c карты",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.cancelMarkOnMap)) {
                            mapObjects.clear()
                            binding.locationCurrentAddMarker.setImageResource(R.drawable.us_m2)
                        }
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        marksViewModel.deleteMark(
                            Mark(
                                0,
                                locMark!!.position.longitude,
                                locMark!!.position.latitude
                            )
                        )
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        marksViewModel.marks.collect { marks ->
                            if (marks != null) {
                                marks.forEach {
                                    mapObjects.addPlacemark(
                                        Point(it.coordinateLat, it.coordinateLong),
                                        ImageProvider.fromResource(
                                            requireContext(),
                                            R.drawable.us_m2
                                        )
                                    )
                                }

                            }
                        }
                    }
                    snackbar.setActionTextColor(Color.WHITE)
                    snackbar.setBackgroundTint((Color.RED))

                        .show()

                }
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

        }
        SearchFactory.initialize(requireContext())
        searchManager =
            SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        binding.mapview.map.addCameraListener(this)
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

    override fun onResume() {
        super.onResume()
        val mapObjects = binding.mapview.map.mapObjects
        Handler().postDelayed({
            if (loc == null) {
                getLocation()
            }
        }, 30000)
        Handler().postDelayed({
            if (loc != null) {
                activity?.runOnUiThread {
                    binding.zoombtn.isEnabled = true
                    binding.zoombtndec.isEnabled = true
                    binding.locationCurrentAddMarker.isEnabled = true
                    binding.sendLocation.isEnabled = true
                    binding.searchField.isEnabled = true
                    binding.prob.isEnabled = true
                    binding.userlocation.isEnabled = true
                    binding.delallmarks.isEnabled = true
                }
            }
        }, 32000)
        viewLifecycleOwner.lifecycleScope.launch {
            marksViewModel.marks.collect { marks ->
                if (marks != null) {
                    marks.forEach {
                        mapObjects.addPlacemark(
                            Point(it.coordinateLat, it.coordinateLong),
                            ImageProvider.fromResource(requireContext(), R.drawable.us_m2)
                        )
                    }

                }
            }
        }
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
                        val intent = Intent().setClassName(
                            requireContext(),
                            "home.howework.panoramafeature.PanoramaActivityF"
                        )
                        intent.putExtra("lat", p1.latitude)
                        intent.putExtra("long", p1.longitude)
                        startActivity(intent)
                        // activity?.finish()
                    }

            }

            override fun onMapLongTap(p0: com.yandex.mapkit.map.Map, p1: Point) {
            }
        }
        return inputListener
    }

    override fun onStop() {
        binding.mapview.onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        binding.mapview.map.addInputListener(inputListenerOnMap())
        binding.mapview.map.isRotateGesturesEnabled = true
        binding.mapview.map.isScrollGesturesEnabled = true
        binding.mapview.onStart()
        if (loc == null) {
            getLocation()
        }
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
            Animation(Animation.Type.SMOOTH, 0.0f), null
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
            Animation(Animation.Type.SMOOTH, 0.0f), null
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


    private fun localListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                loc = location
                Toast.makeText(
                    requireContext(),
                    getString(R.string.yourCoordinates) + ": ${location.position.latitude} и ${location.position.longitude}",
                    Toast.LENGTH_LONG
                ).show()

                myLocation = location.position
                if (myLocation != null) {
                    ApplicationMapKit.LocalHelp.latitudeActitvity = location.position.latitude
                    ApplicationMapKit.LocalHelp.longitudeActivity = location.position.longitude
                    turnButtons()

                }
            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {
            }
        }
    }

    private fun turnButtons() {
        if (loc != null) {
            binding.zoombtn.isEnabled = true
            binding.zoombtndec.isEnabled = true
            binding.locationCurrentAddMarker.isEnabled = true
            binding.sendLocation.isEnabled = true
            binding.searchField.isEnabled = true
            binding.prob.isEnabled = true
            binding.userlocation!!.isEnabled = true
            binding.delallmarks.isEnabled=true
            ApplicationMapKit.LocalHelp.offOnUserLayer = true
        }
    }

    private fun getLocation() {
        locationManager.requestSingleUpdate(
            localListener()
        )
    }


    override fun onSearchResponse(response: Response) {
        if (binding.searchField.text.isNotEmpty()) {
            val mapObjects = binding.mapview.map.mapObjects
            mapObjects.clear()
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
            if (binding.searchField.text.isNotEmpty())
                queryPlace(binding.searchField.text.toString())
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
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                requireContext(),
                R.drawable.user_arrow_alt2
            )
        )
        val picIcon = userLocationView.pin.useCompositeIcon()
        picIcon.setIcon(
            "Icon", ImageProvider.fromResource(requireContext(), R.drawable.search_result),
            IconStyle().setRotationType(RotationType.NO_ROTATION).setZIndex(0f).setScale(1f)
        )
        picIcon.setIcon(

            "pin", ImageProvider.fromResource(requireContext(), R.drawable.nothing),
            IconStyle().setAnchor(PointF(0.5f, 05f)).setRotationType(RotationType.ROTATE)
                .setZIndex(1f).setScale(0.5f)
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001


    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }

}
