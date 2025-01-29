package com.riog.accel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationsFragment : Fragment() {
    private lateinit var locationsRecyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var deleteButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var locationsAdapter: LocationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_locations_list, container, false)
        
        locationsRecyclerView = view.findViewById(R.id.locationsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        deleteButton = view.findViewById(R.id.deleteLocationsButton)
        
        databaseHelper = DatabaseHelper(requireContext())
        
        setupRecyclerView()
        setupDeleteButton()
        
        return view
    }

    private fun setupRecyclerView() {
        val locations = databaseHelper.getAllLocations()
        
        locationsAdapter = LocationsAdapter(locations)
        locationsRecyclerView.layoutManager = LinearLayoutManager(context)
        locationsRecyclerView.adapter = locationsAdapter
        
        updateEmptyViewVisibility(locations)
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
            
            locationsAdapter = LocationsAdapter(locations)
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

class LocationsAdapter(private val locations: List<LocationEntry>) : 
    RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val latitudeTextView: TextView = view.findViewById(R.id.latitudeTextView)
        val longitudeTextView: TextView = view.findViewById(R.id.longitudeTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        
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
    }

    override fun getItemCount() = locations.size
}