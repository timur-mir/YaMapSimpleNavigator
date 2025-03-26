package com.example.location

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.example.location.ApplicationMapKit.LocalHelp.lastIdValue
import com.example.location.ApplicationMapKit.LocalHelp.loc
import com.example.location.ApplicationMapKit.LocalHelp.locMark
import com.example.location.ApplicationMapKit.LocalHelp.markAdd
import com.example.location.ApplicationMapKit.LocalHelp.marksSize
import com.example.location.ApplicationMapKit.LocalHelp.myLocation
import com.example.location.ApplicationMapKit.LocalHelp.offOn
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
import com.example.location.PanoramaPlaceFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.location.ApplicationMapKit.LocalHelp.activityClose
import com.yandex.mapkit.GeoObject

class MainFragment : Fragment(), com.yandex.mapkit.search.Session.SearchListener, CameraListener,
    UserLocationObjectListener,Transaction{

    private var _binding: MainFragmentBinding? = null
    val binding get() = _binding!!
    lateinit var searchSession: com.yandex.mapkit.search.Session
    lateinit var searchManager: SearchManager
    lateinit var locationManager: LocationManager
    lateinit var splitInstallManager: SplitInstallManager
    lateinit var locationmapkit: UserLocationLayer
    lateinit var  panoramaPlaceFragment:PanoramaPlaceFragment
    lateinit var geocoder: Geocoder
    var lat: Double = 0.0
    var lon: Double = 0.0
    var azimuth: Float = 0.0f
    var tilt: Float = 0.0f
    var zoom: Float = 0.0f
    lateinit  var toast:Toast
    private val RECOGNIZER_RESULT = 1234
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
        geocoder=Geocoder(requireActivity())
        val
        val mapKit: MapKit = MapKitFactory.getInstance()
        val probki = mapKit.createTrafficLayer(binding.mapview.mapWindow)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        getLocation()
        binding.userlocation!!.setOnClickListener {
            getLocation()
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
            getLocation()
            loc?.let { location ->
                val mapObjects = binding.mapview.map.mapObjects
                locMark = location
                val snackbar = Snackbar.make(
                    binding.root,
                    "Маркер на карту",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.addMark)) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            marksViewModel.addMark(
                                Mark(
                                    marksSize+1,
                                    locMark!!.position.longitude,
                                    locMark!!.position.latitude
                                )
                            )
                            marksSize+=1
                        }
                        mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(requireContext(), R.drawable.us_m22)
                        )

                    }
                snackbar.setActionTextColor(Color.WHITE)
                snackbar.setBackgroundTint((Color.BLUE))

                    .show()
            }
        }
        binding.locationCurrentDeleteMarker.setOnClickListener{
            val mapObjects = binding.mapview.map.mapObjects
            if (marksSize>0) {

                val snackbar = Snackbar.make(
                    binding.root,
                    "Убрать c карты",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.cancelMarkOnMap)) {
                        mapObjects.clear()

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            marksViewModel.deleteMark(
                                marksSize,
                            )
                            if(marksSize>0) {
                                marksSize -= 1
                            }
                        }
                    }


                viewLifecycleOwner.lifecycleScope.launch {
                    marksViewModel.getAllMarks()
                    delay(300)
                    marksViewModel.marks2.collect { marks ->
                        val mapObjectsInner = binding.mapview.map.mapObjects
                        if (marks != null) {
                            marks.forEach {
                                mapObjectsInner.addPlacemark(
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

        binding.voicesearch.setOnClickListener{
            Toast.makeText(
                requireContext(),
                " Попробуйте голосовой поиск...",
                Toast.LENGTH_LONG
            ).show()
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
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK) {
            val matches: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ApplicationMapKit.LocalHelp.speachText= matches?.get(0)?.toString() ?: "Деловой центр"
            matches?.get(0)?.toString()?.let { queryPlace(matches?.get(0)?.toString() ?: "Театральная") }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    override fun onResume() {
        super.onResume()
        val mapObjects = binding.mapview.map.mapObjects
        Handler().postDelayed({
            if (loc == null) {
                getLocation()
            }
        }, 5000)
        Handler().postDelayed({
            if (loc != null) {
                activity?.runOnUiThread {
                    binding.voicesearch.isEnabled=true
                    binding.zoombtn.isEnabled = true
                    binding.zoombtndec.isEnabled = true
                    binding.locationCurrentAddMarker.isEnabled = true
                    binding.sendLocation.isEnabled = true
                    binding.searchField.isEnabled = true
                    binding.prob.isEnabled = true
                    binding.userlocation.isEnabled = true
                    binding.delallmarks.isEnabled = true
                    binding.locationCurrentDeleteMarker.isEnabled=true
                }
            }
        }, 6000)
        viewLifecycleOwner.lifecycleScope.launch {
            marksViewModel.marks.collect { marks ->
                if (marks != null) {
                    marksSize=marks.size
                    //  lastIdValue=marks.lastIndexOf(marks[marksSize])
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
                        activityClose=true
                        val intent = Intent().setClassName(
                            requireContext(),
                            "home.howework.panoramafeature.PanoramaActivityF"
                        )
                        intent.putExtra("lat", p1.latitude)
                        intent.putExtra("long", p1.longitude)
                        intent.flags=(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
        binding.searchField.setText("")
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
                geocoder=Geocoder(requireActivity())
                var town=geocoder.getFromLocation(location.position.latitude, location.position.longitude,1)
                binding.localInfo.text= town!![0].adminArea.toString()
                if(isAdded) {
                    toast=
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.yourCoordinates) + ": ${location.position.latitude} и ${location.position.longitude}" +
                                    " город ${town!![0].adminArea}",
                            Toast.LENGTH_LONG
                        )
                    toast.show()
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
        if(::toast.isInitialized){
            toast.cancel()
        }
        _binding=null

    }
    private fun turnButtons() {
        if (loc != null) {
            binding.voicesearch.isEnabled=true
            binding.zoombtn.isEnabled = true
            binding.zoombtndec.isEnabled = true
            binding.locationCurrentAddMarker.isEnabled = true
            binding.sendLocation.isEnabled = true
            binding.searchField.isEnabled = true
            binding.prob.isEnabled = true
            binding.userlocation!!.isEnabled = true
            binding.delallmarks.isEnabled=true
            ApplicationMapKit.LocalHelp.offOnUserLayer = true
            binding.locationCurrentDeleteMarker.isEnabled=true
        }
    }

    private fun getLocation() {
        locationManager.requestSingleUpdate(
            localListener()
        )
    }


    override fun onSearchResponse(response: Response) {
        val args = Bundle()
        var needCoordinatesPointer=0
        if (binding.searchField.text.isNotEmpty()|| ApplicationMapKit.LocalHelp.speachText.isNotEmpty()) {
            Toast.makeText(requireContext(), "Нормально ${response.metadata.toponym}", Toast.LENGTH_SHORT).show()
            if (binding.searchField.text.isNotEmpty())
                {
                binding.searchField.text.clear()
            }
            ApplicationMapKit.LocalHelp.speachText=""
            val mapObjects = binding.mapview.map.mapObjects
            mapObjects.clear()
            if(response.collection.children.size>1&&response.collection.children.size%2==0){
                needCoordinatesPointer=response.collection.children.size/2
                val snackbar=Snackbar.make(
                    binding.root,
                    "Посмотрите на результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.argb(100,252,63,29))
                snackbar.setBackgroundTint((Color.WHITE))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(Point(response.collection.children[0].obj!!.geometry[0].point!!.latitude,
                        response.collection.children[0].obj!!.geometry[0].point!!.longitude ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt),
                    Animation(Animation.Type.SMOOTH, 1.0f), null )

                val menuItem=  requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(0)
                menuItem.title=resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity = response.collection.children[needCoordinatesPointer].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity = response.collection.children[needCoordinatesPointer].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch (Dispatchers.Main) {
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
            else if(response.collection.children.size==1){
                val snackbar=Snackbar.make(
                    binding.root,
                    "Посмотрите на результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.argb(100,252,63,29))
                snackbar.setBackgroundTint((Color.WHITE))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(Point(response.collection.children[0].obj!!.geometry[0].point!!.latitude,
                        response.collection.children[0].obj!!.geometry[0].point!!.longitude ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt),
                    Animation(Animation.Type.SMOOTH, 1.0f), null )

                val menuItem=  requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(0)
                menuItem.title=resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity = response.collection.children[0].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity = response.collection.children[0].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch (Dispatchers.Main) {
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
            else if(response.collection.children.size>2){
             val snackbar=Snackbar.make(
                    binding.root,
                    "Посмотрите на результат  поиска...${response.metadata.requestText} ",
                    Snackbar.LENGTH_LONG
                )
                snackbar.setTextColor(Color.argb(100,252,63,29))
                snackbar.setBackgroundTint((Color.WHITE))
                    .show()
                binding.mapview.map.move(
                    CameraPosition(Point(response.collection.children[2].obj!!.geometry[0].point!!.latitude,
                        response.collection.children[2].obj!!.geometry[0].point!!.longitude ),
                        binding.mapview.map.cameraPosition.zoom,
                        binding.mapview.map.cameraPosition.azimuth,
                        binding.mapview.map.cameraPosition.tilt),
                    Animation(Animation.Type.SMOOTH, 1.0f), null )

                val menuItem=  requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).menu.getItem(0)
                menuItem.title=resources.getString(R.string.panorama_look)
                requireActivity().findViewById<BottomNavigationView>(R.id.panel_navigation_main).setBackgroundColor(resources.getColor(R.color.bottom2))
                ApplicationMapKit.LocalHelp.latitudeActivity = response.collection.children[2].obj!!.geometry[0].point!!.latitude
                ApplicationMapKit.LocalHelp.longitudeActivity = response.collection.children[2].obj!!.geometry[0].point!!.longitude
                lifecycleScope.launch (Dispatchers.Main) {
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

    override fun navigateTo(fragment: Fragment) {
        TODO("Not yet implemented")
    }


}
