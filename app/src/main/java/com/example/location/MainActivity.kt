package com.example.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.location.ApplicationMapKit.LocalHelp.loc
import com.example.location.ApplicationMapKit.LocalHelp.locMark
import com.example.location.ApplicationMapKit.LocalHelp.markAdd
import com.example.location.ApplicationMapKit.LocalHelp.myLocation
import com.example.location.ApplicationMapKit.LocalHelp.offOn
import com.example.location.R.id.location_current_add_marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
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
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.NotFoundError
import com.yandex.runtime.network.RemoteError

private const val KEY_LAT = "lat"
private const val KEY_LON = "lon"
private const val KEY_AZIMUTH = "azimuth"
private const val KEY_TILT = "tilt"
private const val KEY_ZOOM = "zoom"

class MainActivity : AppCompatActivity(),
    com.yandex.mapkit.search.Session.SearchListener, CameraListener {
    var lat: Double = 0.0
    var lon: Double = 0.0
    var azimuth: Float = 0.0f
    var tilt: Float = 0.0f
    var zoom: Float = 0.0f
    private val TAG: String? = MainActivity::class.java.simpleName
    lateinit var probbt: FloatingActionButton
    lateinit var geoPosition: FloatingActionButton
    lateinit var zoombuton: FloatingActionButton
    lateinit var zoombutondec: FloatingActionButton
    lateinit var sendLocation: FloatingActionButton
    lateinit var currentLocationAddMarker: FloatingActionButton
    lateinit var searchField: EditText
    lateinit var mapview: MapView
    lateinit var searchSession: com.yandex.mapkit.search.Session
    lateinit var searchManager: SearchManager
    lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()

        setContentView(R.layout.activity_main);
        mapview = findViewById<MapView>(R.id.mapview)
        probbt = findViewById(R.id.prob)
        geoPosition = findViewById(R.id.geo_position_btn)
        zoombuton = findViewById(R.id.zoombtn)
        zoombutondec = findViewById(R.id.zoombtndec)
        sendLocation = findViewById(R.id.sendLocation)
        currentLocationAddMarker = findViewById<FloatingActionButton>(location_current_add_marker)
        searchField = findViewById(R.id.search_field)


        requestLocationPermission()
        val mapKit: MapKit = MapKitFactory.getInstance()
        val probki = mapKit.createTrafficLayer(mapview.mapWindow)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        getLocation()
        //Для добавления кода при повороте
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (locMark != null) {

                    if (markAdd == true) {
                        mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(this, R.drawable.us_m2)
                        )
                        currentLocationAddMarker.setImageResource(R.drawable.us_m22)
                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                if (locMark != null) {

                    if (markAdd == true) {
                        mapview.map.mapObjects.addPlacemark(
                            locMark!!.position,
                            ImageProvider.fromResource(this, R.drawable.us_m2)
                        )
                        currentLocationAddMarker.setImageResource(R.drawable.us_m22)
                    }
                }
            }

            else -> {
            }
        }
        Toast.makeText(
            this@MainActivity,
            "Загрузка карт...Определяется местоположение по местности...",
            Toast.LENGTH_LONG
        ).show()
        zoombuton.setOnClickListener {
            myLocation?.let { it1 -> zoom(0, it1) }
        }
        zoombutondec.setOnClickListener {
            myLocation?.let { it1 -> zoom(1, it1) }
        }
        sendLocation.setOnClickListener {
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
        currentLocationAddMarker.setOnClickListener {
            if (loc == null) {
                getLocation()
            }
            loc?.let { location ->
                val mapObjects = mapview.map.mapObjects
                var constraintLayout =
                    findViewById<ConstraintLayout>(R.id.custom_snackbar_container)
                if (!markAdd) {
                    locMark = location
                    markAdd = true
                    val snackbar = Snackbar.make(
                        constraintLayout,
                        "Маркер на карту",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.addMark)) {
                            mapObjects.addPlacemark(
                                locMark!!.position,
                                ImageProvider.fromResource(this, R.drawable.us_m2)
                            )
                            currentLocationAddMarker.setImageResource(R.drawable.us_m22)
                        }
                    snackbar.setActionTextColor(Color.WHITE)
                    snackbar.setBackgroundTint((Color.BLUE))

                        .show()
                } else if (markAdd) {
                    markAdd = false
                    val snackbar = Snackbar.make(
                        constraintLayout,
                        "Убрать c карты",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.cancelMarkOnMap)) {
                            mapObjects.clear()
                            currentLocationAddMarker.setImageResource(R.drawable.us_m2)
                        }
                    snackbar.setActionTextColor(Color.WHITE)
                    snackbar.setBackgroundTint((Color.RED))

                        .show()

                }
            }
        }

        probbt.setOnClickListener {
            if (offOn == false) {
                probki.isTrafficVisible = true
                offOn = true
            } else {
                probki.isTrafficVisible = false
            }

        }
        geoPosition.setOnClickListener {
            getLocation()

        }
        SearchFactory.initialize(this)
        searchManager =
            SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        mapview.map.addCameraListener(this)
        searchField.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                queryPlace(searchField.text.toString())
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

    private fun turnButtons() {
        if (loc != null) {
            zoombuton.isEnabled = true
            zoombutondec.isEnabled = true
            currentLocationAddMarker.isEnabled = true
            sendLocation.isEnabled = true
            searchField.isEnabled = true
            probbt.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({
            if (loc == null) {
                getLocation()
            }
        }, 30000)
        Handler().postDelayed({
            if (loc != null) {
                runOnUiThread {
                    zoombuton.isEnabled = true
                    zoombutondec.isEnabled = true
                    currentLocationAddMarker.isEnabled = true
                    sendLocation.isEnabled = true
                    searchField.isEnabled = true
                    probbt.isEnabled = true

                }
            }
        }, 32000)
    }

    private fun inputListenerOnMap(): InputListener {
        val inputListener: InputListener = object : InputListener {
            override fun onMapTap(p0: Map, p1: Point) {
                val i = Intent(this@MainActivity, PanoramaActivity::class.java)
                i.putExtra("lat", p1.latitude)
                i.putExtra("long", p1.longitude)
                startActivity(i)
            }

            override fun onMapLongTap(p0: com.yandex.mapkit.map.Map, p1: Point) {
            }
        }
        return inputListener
    }

    override fun onStop() {
        mapview.onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        mapview.map.addInputListener(inputListenerOnMap())
        mapview.map.isRotateGesturesEnabled = true
        mapview.map.isScrollGesturesEnabled = true
        mapview.onStart()
        if (loc == null) {
            getLocation()
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        if (mapview.map.isValid) {
            bundle.putDouble(KEY_LAT, mapview.map.cameraPosition.target.latitude)
            bundle.putDouble(KEY_LON, mapview.map.cameraPosition.target.longitude)
            bundle.putFloat(KEY_AZIMUTH, mapview.map.cameraPosition.azimuth)
            bundle.putFloat(KEY_TILT, mapview.map.cameraPosition.tilt)
            bundle.putFloat(KEY_ZOOM, mapview.map.cameraPosition.zoom)
            super.onSaveInstanceState(bundle)
        }
    }

    private fun localListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                loc = location
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.yourCoordinates) + ": ${location.position.latitude} и ${location.position.longitude}",
                    Toast.LENGTH_LONG
                ).show()
                myLocation = location.position
                if (myLocation != null) {
                    turnButtons()
                }
            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {
            }
        }
    }

    private fun getLocation() {
        locationManager.requestSingleUpdate(
            localListener()
        )
    }

    private fun setLocation() {
        mapview.map.move(
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
        mapview.map.move(
            CameraPosition(Point(lat, lon), zoom, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, 0.0f), null
        )
    }

    private fun queryPlace(query: String) {
        searchSession = searchManager.submit(
            query,
            VisibleRegionUtils.toPolygon(mapview.map.visibleRegion),
            SearchOptions(),
            this
        )
    }

    private fun zoom(sender: Int, point: Point) {
        val zoomStep: Float = if (sender == 0) -1f else 1f
        val position = CameraPosition(point, mapview.map.cameraPosition.zoom + zoomStep, 0F, 0F)
        mapview.map.move(
            position, Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 0
            )
        return
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) {
            if (searchField.text.isNotEmpty())
                queryPlace(searchField.text.toString())
        }
    }

    override fun onSearchResponse(response: Response) {
        if (searchField.text.isNotEmpty()) {
            val mapObjects = mapview.map.mapObjects
            mapObjects.clear()
            for (searchResult in response.collection.children) {
                val resultLocation = searchResult.obj!!.geometry[0].point!!
                if (response != null) {
                    mapObjects.addPlacemark(
                        resultLocation,
                        ImageProvider.fromResource(this, R.drawable.search_result)
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
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }


}





