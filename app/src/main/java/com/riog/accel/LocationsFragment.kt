package com.riog.accel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationsFragment : Fragment() {
    private lateinit var locationsRecyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var deleteButton: Button
    private lateinit var addLocationButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations_list, container, false)
        
        locationsRecyclerView = view.findViewById(R.id.locationsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        deleteButton = view.findViewById(R.id.deleteLocationsButton)
        addLocationButton = view.findViewById(R.id.addLocationButton)
        
        databaseHelper = DatabaseHelper(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        setupRecyclerView()
        setupDeleteButton()
        setupAddLocationButton()
        
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location permission is required to add locations",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun refreshLocationsList() {
        val locations = databaseHelper.getAllLocations()
        locationsAdapter = LocationsAdapter(locations) { location ->
            shareLocation(location)
        }
        locationsRecyclerView.adapter = locationsAdapter
        updateEmptyViewVisibility(locations)
    }

    private fun setupRecyclerView() {
        val locations = databaseHelper.getAllLocations()
        
        locationsAdapter = LocationsAdapter(locations) { location ->
            shareLocation(location)
        }
        locationsRecyclerView.layoutManager = LinearLayoutManager(context)
        locationsRecyclerView.adapter = locationsAdapter
        
        updateEmptyViewVisibility(locations)
    }

    // New method to share location
    private fun shareLocation(location: LocationEntry) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Location Details")
            
            // Create a formatted location message
            val locationMessage = buildString {
                append("Location Details:\n")
                append("Latitude: ${location.latitude}\n")
                append("Longitude: ${location.longitude}\n")
                
                // Add address if available
                val addressParts = listOfNotNull(
                    location.streetNumber,
                    location.streetName,
                    location.city,
                    location.state,
                    location.postalCode,
                    location.country
                )
                if (addressParts.isNotEmpty()) {
                    append("Address: ${addressParts.joinToString(", ")}\n")
                }
                
                // Add Google Maps link
                append("\nView on Google Maps: https://www.google.com/maps?q=${location.latitude},${location.longitude}")
            }
            
            putExtra(Intent.EXTRA_TEXT, locationMessage)
        }
        
        // Start the share intent
        startActivity(Intent.createChooser(shareIntent, "Share Location"))
    }

    private fun setupAddLocationButton() {
        addLocationButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                getCurrentLocation()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // Save location to database
                        databaseHelper.insertLocation(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                        
                        // Refresh the locations list
                        refreshLocationsList()
                        
                        Toast.makeText(
                            requireContext(),
                            "Location saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            "Could not get current location. Please ensure location is enabled.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error getting location: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun updateEmptyViewVisibility(locations: List<LocationEntry>) {
        if (locations.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            locationsRecyclerView.visibility = View.GONE
            deleteButton.isEnabled = false
        } else {
            emptyView.visibility = View.GONE
            locationsRecyclerView.visibility = View.VISIBLE
            deleteButton.isEnabled = true
        }
    }

    private fun setupDeleteButton() {
        deleteButton.setOnClickListener {
            databaseHelper.dropLocationsTable()
            val locations = databaseHelper.getAllLocations()
            
            locationsAdapter = LocationsAdapter(locations) { location ->
                shareLocation(location)
            }
            locationsRecyclerView.adapter = locationsAdapter
            
            updateEmptyViewVisibility(locations)
            
            Toast.makeText(
                requireContext(),
                "Dropped and recreated locations table",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        databaseHelper.close()
    }
}

class LocationsAdapter(
    private val locations: List<LocationEntry>,
    private val onShareClick: (LocationEntry) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemNumberTextView: TextView = view.findViewById(R.id.itemNumberTextView)
        val latitudeTextView: TextView = view.findViewById(R.id.latitudeTextView)
        val longitudeTextView: TextView = view.findViewById(R.id.longitudeTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val shareButton: ImageButton = view.findViewById(R.id.shareButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        
        // Set item number
        holder.itemNumberTextView.text = "#${position + 1}"
        
        holder.latitudeTextView.text = String.format("Latitude: %.6f", location.latitude)
        holder.longitudeTextView.text = String.format("Longitude: %.6f", location.longitude)
        
        // Format address, handling potential null values
        val addressParts = listOfNotNull(
            location.streetNumber,
            location.streetName,
            location.city,
            location.state,
            location.postalCode,
            location.country
        )
        holder.addressTextView.text = if (addressParts.isNotEmpty()) {
            addressParts.joinToString(", ")
        } else {
            "No address information"
        }
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(location.timestamp))
        holder.timestampTextView.text = "Recorded: $formattedDate"
        
        // Set up share button
        holder.shareButton.setOnClickListener {
            onShareClick(location)
        }
    }

    override fun getItemCount() = locations.size
}