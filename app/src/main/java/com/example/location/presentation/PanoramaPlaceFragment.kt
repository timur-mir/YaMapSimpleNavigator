package com.example.location.presentation

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.location.LatLong
import com.example.location.databinding.PanoramaPlaceFragmentBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.places.PlacesFactory
import com.yandex.mapkit.places.panorama.PanoramaService
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.NotFoundError
import com.yandex.runtime.network.RemoteError

import home.howework.panorama.R

private const val LAT = "lat"
private const val LON = "lon"
class PanoramaPlaceFragment: Fragment(), PanoramaService.SearchListener {
    lateinit var SEARCH_LOCATION: Point
    lateinit var panoramaService: PanoramaService
    lateinit var searchSession: PanoramaService.SearchSession
    private var _binding: PanoramaPlaceFragmentBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PanoramaPlaceFragmentBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PlacesFactory.initialize(requireContext())
        val mArgs = arguments
        val objLatLong= mArgs?.getSerializable("lat-long") as LatLong
        SEARCH_LOCATION = Point(objLatLong.lat,objLatLong.long)
        panoramaService = PlacesFactory.getInstance().createPanoramaService();
        searchSession = panoramaService.findNearest(SEARCH_LOCATION, this);

    }
    override fun onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        binding.panoramaViewPlace.onStart();
        super.onStart()
    }
    override fun onStop() {
        binding.panoramaViewPlace.onStop();
        MapKitFactory.getInstance().onStop();
        activity?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        super.onStop()
    }


    override fun onPanoramaSearchResult(panoramaId: String) {
        binding.panoramaViewPlace.player?.openPanorama(panoramaId);
        binding.panoramaViewPlace.player?.enableMove();
        binding.panoramaViewPlace.player?.enableRotation();
        binding.panoramaViewPlace.player?.enableZoom();
        binding.panoramaViewPlace.player?.enableMarkers();
    }

    override fun onPanoramaSearchError(error: Error) {
        var errorMessage = "";
        if (error is NotFoundError) {
            errorMessage = getString(R.string.notFoundError3)
        } else if (error is RemoteError) {
            errorMessage =  getString(R.string.remoteError3)
        } else if (error is NetworkError) {
            errorMessage =  getString(R.string.networkError3)
        }

//        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }
    companion object {

        fun newInstance(lat:Double,lon:Double ): PanoramaPlaceFragment {
            val args = Bundle().apply {
                val obj= LatLong(lat,lon)
                putSerializable("lat-long",obj)
            }
            return PanoramaPlaceFragment().apply {
                arguments=args
            }
        }


    }
}