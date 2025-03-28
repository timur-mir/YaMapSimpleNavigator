package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.location.ApplicationMapKit.LocalHelp.activityClose
import com.example.location.ApplicationMapKit.LocalHelp.latitudeActivity
import com.example.location.ApplicationMapKit.LocalHelp.latitudeDeviceOldPosition
import com.example.location.ApplicationMapKit.LocalHelp.longitudeActivity
import com.example.location.ApplicationMapKit.LocalHelp.longitudeDeviceOldPosition
import com.example.location.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), Transaction {
    private var _binding:ActivityMainBinding?=null
    private val binding get() = _binding!!
    private val BEGIN = Menu.FIRST
    private val BACK = BEGIN + 1
    lateinit var panoramaPlaceFragment: PanoramaPlaceFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        panoramaPlaceFragment =
            PanoramaPlaceFragment.newInstance(latitudeActivity, longitudeActivity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        supportActionBar?.hide()
        requestLocationPermission()
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

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = binding.panelNavigationMain
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
//        val navController: NavController = navHostFragment.navController
        val navController = findNavController(R.id.navHostFragment)
        val args = Bundle()
        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (activityClose) {

                }
                if (navController.graph.startDestination == navController.currentDestination?.id) {
                    finish()
                } else {
                    navController.popBackStack()
                    panoramaPlaceFragment =
                        PanoramaPlaceFragment.newInstance(latitudeActivity, longitudeActivity)
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.small_navHostFragment, panoramaPlaceFragment)
                    transaction.addToBackStack("panorama")
                    transaction.commit()
                    bottomNavigationView.menu.removeItem(BACK)
                    bottomNavigationView.menu.add(
                        Menu.NONE, R.id.panoramaFragment, Menu.NONE,
                        R.string.panorama
                    ).setIcon(R.drawable.panorama)
                    bottomNavigationView.setBackgroundColor(resources.getColor(R.color.bottom1))
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            val obj = LatLong(
                ApplicationMapKit.LocalHelp.latitudeActivity,
                ApplicationMapKit.LocalHelp.longitudeActivity
            )
            args.putSerializable("lat-long", obj)
            when (it.itemId) {
                R.id.panoramaFragment -> {
                    binding.smallNavHostFragment.isEnabled = false
                    binding.smallNavHostFragment.visibility = View.INVISIBLE
                    navController.navigate(R.id.panoramaFragmentFeature, args)
                    bottomNavigationView.menu.removeItem(R.id.panoramaFragment)
                    bottomNavigationView.menu.add(
                        Menu.NONE, BACK, Menu.NONE,
                        R.string.back
                    ).setIcon(R.drawable.back)
                    bottomNavigationView.setBackgroundColor(resources.getColor(R.color.bottom3))
                }

                BACK -> {
                    binding.smallNavHostFragment.isEnabled = true
                    binding.smallNavHostFragment.visibility = View.VISIBLE
                    navController.popBackStack()
                    panoramaPlaceFragment =
                        PanoramaPlaceFragment.newInstance(latitudeActivity, longitudeActivity)
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.small_navHostFragment, panoramaPlaceFragment)
                    transaction.addToBackStack("panorama")
                    transaction.commit()
                    bottomNavigationView.menu.removeItem(BACK)
                    bottomNavigationView.menu.add(
                        Menu.NONE, R.id.panoramaFragment, Menu.NONE,
                        R.string.panorama
                    ).setIcon(R.drawable.panorama)
                    bottomNavigationView.setBackgroundColor(resources.getColor(R.color.bottom1))
                }
            }
            true
        }

        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.small_navHostFragment, panoramaPlaceFragment)
        transaction.addToBackStack("panorama")
        transaction.commit()
    }


    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
    override fun navigateTo(fragment: Fragment) {
        binding.smallNavHostFragment.isEnabled = true
        binding.smallNavHostFragment.visibility = View.VISIBLE
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.small_navHostFragment, fragment)
        transaction.commit()
    }

}





