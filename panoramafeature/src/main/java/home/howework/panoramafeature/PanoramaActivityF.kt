package home.howework.panoramafeature

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.location.presentation.ApplicationMapKit

import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.NotFoundError
import com.yandex.runtime.network.RemoteError
import home.howework.panoramafeature.databinding.ActivityfPanoramaBinding

class PanoramaActivityF : AppCompatActivity(), PanoramaService.SearchListener {
    lateinit var SEARCH_LOCATION: Point
    lateinit var panoramaService: PanoramaService
    lateinit var searchSession: PanoramaService.SearchSession
    private var _binding: ActivityfPanoramaBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlacesFactory.initialize(this)
        _binding = ActivityfPanoramaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val extras: Bundle? = intent.extras;
        if (extras != null) {
            var lat: Double = extras.getDouble("lat");
            var long: Double = extras.getDouble("long");
            SEARCH_LOCATION = Point(lat, long)
            panoramaService = PlacesFactory.getInstance().createPanoramaService();
            searchSession = panoramaService.findNearest(SEARCH_LOCATION, this);
        }
    }
    override fun onStart() {
        super.onStart();
        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (ApplicationMapKit.LocalHelp.activityClose) {
                    val intent = Intent().setClassName(
                       this@PanoramaActivityF,
                        "com.example.location.presentation.MainActivity"
                    )
                    ApplicationMapKit.LocalHelp.activityClose=false
                    intent.flags=(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
            }

        }
        this.onBackPressedDispatcher.addCallback(this, callback)
        MapKitFactory.getInstance().onStart();
        binding.panoramaViewf.onStart();
        super.onStart()
    }
    override fun onStop() {
        binding.panoramaViewf.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop()
    }


    override fun onPanoramaSearchResult(panoramaId: String) {
        binding.panoramaViewf.player?.openPanorama(panoramaId);
        binding.panoramaViewf.player?.enableMove();
        binding.panoramaViewf.player?.enableRotation();
        binding.panoramaViewf.player?.enableZoom();
        binding.panoramaViewf.player?.enableMarkers();
    }

    override fun onPanoramaSearchError(error: Error) {
        var errorMessage = "";
        if (error is NotFoundError) {
            errorMessage = getString(R.string.notFoundError2)
        } else if (error is RemoteError) {
            errorMessage =  getString(R.string.remoteError2)
        } else if (error is NetworkError) {
            errorMessage =  getString(R.string.networkError2)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
    }
