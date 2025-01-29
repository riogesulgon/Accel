package com.riog.accel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val DATABASE_NAME = "LocationTracker.db"
    }

    private var databaseHelper: DatabaseHelper? = null
    private var viewPager: ViewPager2? = null
    private var tabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "Starting onCreate")
            
            // Delete the SQLite database during startup
            // Useful for testing.
            // deleteDatabase(DATABASE_NAME)

            setContentView(R.layout.activity_main)
            
            // Initialize DatabaseHelper
            databaseHelper = DatabaseHelper(this)

            // Initialize ViewPager and TabLayout with comprehensive null checks and logging
            initializeViews()

            // Request location permissions if not granted
            checkLocationPermissions()

            Log.d(TAG, "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during activity creation", e)
            // Optionally show an error dialog to the user
            finish() // Close the activity if initialization fails
        }
    }

    private fun initializeViews() {
        Log.d(TAG, "Initializing views")
        
        // Explicit null checks with detailed logging
        viewPager = findViewById(R.id.viewPager)
        if (viewPager == null) {
            Log.e(TAG, "ViewPager initialization failed: findViewById returned null")
            throw IllegalStateException("ViewPager could not be initialized")
        }

        tabLayout = findViewById(R.id.tabLayout)
        if (tabLayout == null) {
            Log.e(TAG, "TabLayout initialization failed: findViewById returned null")
            throw IllegalStateException("TabLayout could not be initialized")
        }

        // Ensure non-null assertion is safe
        val safeViewPager = viewPager!!
        val safeTabLayout = tabLayout!!

        // Setup ViewPager with fragments
        val pagerAdapter = MainPagerAdapter(this)
        safeViewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager
        TabLayoutMediator(safeTabLayout, safeViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Map"
                1 -> "Locations"
                else -> ""
            }
        }.attach()

        Log.d(TAG, "Views initialized successfully")
    }

    private fun checkLocationPermissions() {
        Log.d(TAG, "Checking location permissions")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Location permissions not granted, requesting...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "Permissions result received")
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permissions granted")
            } else {
                Log.w(TAG, "Location permissions denied")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Close database helper
        databaseHelper?.close()
        Log.d(TAG, "onDestroy called")
    }

    // PagerAdapter for managing fragments
    private inner class MainPagerAdapter(fragmentActivity: FragmentActivity) : 
        FragmentStateAdapter(fragmentActivity) {
        
        override fun getItemCount(): Int {
            Log.d(TAG, "getItemCount called, returning 2")
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            Log.d(TAG, "createFragment called for position: $position")
            return when (position) {
                0 -> {
                    Log.d(TAG, "Creating MapFragment")
                    MapFragment()
                }
                1 -> {
                    Log.d(TAG, "Creating LocationsFragment")
                    LocationsFragment()
                }
                else -> {
                    Log.e(TAG, "Invalid fragment position: $position")
                    throw IllegalArgumentException("Invalid position")
                }
            }
        }
    }
}