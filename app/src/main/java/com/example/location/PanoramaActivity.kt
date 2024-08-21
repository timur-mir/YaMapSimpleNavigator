package com.example.location

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.mapkit.places.panorama.PanoramaService.SearchSession
import com.yandex.mapkit.places.panorama.PanoramaView
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.NotFoundError
import com.yandex.runtime.network.RemoteError


class PanoramaActivity : AppCompatActivity(), PanoramaService.SearchListener {

    lateinit var SEARCH_LOCATION: Point
    lateinit var panoramaView: PanoramaView
    lateinit var panoramaService: PanoramaService
    lateinit var searchSession: SearchSession
    override fun onCreate(savedInstanceState: Bundle?) {
        PlacesFactory.initialize(this)
        setContentView(R.layout.activity_panorama)
        super.onCreate(savedInstanceState)
        val extras: Bundle? = intent.extras;
        if (extras != null) {
            var lat: Double = extras.getDouble("lat");
            var long: Double = extras.getDouble("long");
            SEARCH_LOCATION = Point(lat, long)
            panoramaView = findViewById(R.id.panoview);
            panoramaService = PlacesFactory.getInstance().createPanoramaService();
            searchSession = panoramaService.findNearest(SEARCH_LOCATION, this);
        }
    }

    override fun onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        panoramaView.onStart();
        super.onStart()
    }

    override fun onStop() {
        panoramaView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop()
    }

    override fun onPanoramaSearchResult(panoramaId: String) {
        panoramaView.player?.openPanorama(panoramaId);
        panoramaView.player?.enableMove();
        panoramaView.player?.enableRotation();
        panoramaView.player?.enableZoom();
        panoramaView.player?.enableMarkers();
    }

    override fun onPanoramaSearchError(error: Error) {
        var errorMessage = "";
        if (error is NotFoundError) {
            errorMessage = getString(R.string.notFoundError)
        } else if (error is RemoteError) {
            errorMessage =  getString(R.string.remoteError)
        } else if (error is NetworkError) {
            errorMessage =  getString(R.string.networkError)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
