package com.riog.accel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {
    companion object {
        private const val TAG = "MapFragment"
    }

    private var mapView: MapView? = null
    private var coordinatesTextView: TextView? = null
    private var streetNameTextView: TextView? = null
    private var addLocationButton: FloatingActionButton? = null
    private var googleMap: GoogleMap? = null
    private var databaseHelper: DatabaseHelper? = null
    private var geocoder: Geocoder? = null
    private var currentStreetName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_map, container, false)
            
            // Explicit null checks with detailed logging
            mapView = view.findViewById(R.id.mapView)
            if (mapView == null) {
                Log.e(TAG, "MapView initialization failed: findViewById returned null")
                throw IllegalStateException("MapView could not be initialized")
            }

            coordinatesTextView = view.findViewById(R.id.coordinatesTextView)
            if (coordinatesTextView == null) {
                Log.e(TAG, "CoordinatesTextView initialization failed: findViewById returned null")
                throw IllegalStateException("CoordinatesTextView could not be initialized")
            }

            streetNameTextView = view.findViewById(R.id.streetNameTextView)
            if (streetNameTextView == null) {
                Log.e(TAG, "StreetNameTextView initialization failed: findViewById returned null")
                throw IllegalStateException("StreetNameTextView could not be initialized")
            }

            // New button initialization
            addLocationButton = view.findViewById(R.id.addLocationButton)
            if (addLocationButton == null) {
                Log.e(TAG, "AddLocationButton initialization failed: findViewById returned null")
                throw IllegalStateException("AddLocationButton could not be initialized")
            }
            
            // Setup button click listener
            addLocationButton?.setOnClickListener {
                manuallyAddCurrentLocation()
            }
            
            // Safely initialize dependencies
            databaseHelper = DatabaseHelper(requireContext())
            geocoder = Geocoder(requireContext(), Locale.getDefault())
            
            // Ensure non-null assertion is safe
            val safeMapView = mapView!!
            safeMapView.onCreate(savedInstanceState)
            safeMapView.getMapAsync(this)
            
            Log.d(TAG, "Fragment view created successfully")
            view
        } catch (e: Exception) {
            Log.e(TAG, "Error creating fragment view", e)
            null
        }
    }

    // New method to manually trigger location saving
    private fun manuallyAddCurrentLocation() {
        Log.d(TAG, "Manually adding current location")
        getCurrentLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = true
                getCurrentLocation()
            } else {
                Log.w(TAG, "Location permission not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onMapReady", e)
        }
    }

    private fun getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Location permissions not granted")
                return
            }
            
            LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        updateLocationUI(it)
                        saveLocationToDatabase(it)
                    } ?: Log.w(TAG, "Location is null")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getCurrentLocation", e)
        }
    }

    private fun saveLocationToDatabase(location: Location) {
        try {
            val helper = databaseHelper ?: throw IllegalStateException("DatabaseHelper is null")
            val geocoderInstance = geocoder ?: throw IllegalStateException("Geocoder is null")
            
            // Attempt to get address details using Geocoder
            val addresses = geocoderInstance.getFromLocation(location.latitude, location.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                
                // Extract address components
                val streetNumber = address.subThoroughfare
                val streetName = address.thoroughfare
                val city = address.locality
                val state = address.adminArea
                val postalCode = address.postalCode
                val country = address.countryName
                
                // Store street name for UI update
                currentStreetName = streetName
                
                // Insert location with full address details
                helper.insertLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    streetNumber = streetNumber,
                    streetName = streetName,
                    city = city,
                    state = state,
                    postalCode = postalCode,
                    country = country
                )
            } else {
                // If no address found, insert location with just coordinates
                helper.insertLocation(location.latitude, location.longitude)
            }
            
            // Notify MainActivity that a location has been added
            val activity = requireActivity()
            if (activity is LocationUpdateListener) {
                activity.onLocationAdded()
            } else {
                Log.w(TAG, "Activity does not implement LocationUpdateListener")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving location to database", e)
            // Fallback to inserting just coordinates if geocoding fails
            databaseHelper?.insertLocation(location.latitude, location.longitude)
            
            // Still notify MainActivity even if there was an error
            val activity = requireActivity()
            if (activity is LocationUpdateListener) {
                activity.onLocationAdded()
            } else {
                Log.w(TAG, "Activity does not implement LocationUpdateListener")
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        try {
            val latLng = LatLng(location.latitude, location.longitude)
            
            // Update coordinates text
            coordinatesTextView?.text = "Lat: ${location.latitude}, Lon: ${location.longitude}"
            
            // Update street name text
            streetNameTextView?.text = currentStreetName ?: "Street name not available"
            
            // Update map
            googleMap?.let {
                it.clear()
                it.addMarker(MarkerOptions().position(latLng).title("My Location"))
                it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating location UI", e)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        databaseHelper?.close()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}