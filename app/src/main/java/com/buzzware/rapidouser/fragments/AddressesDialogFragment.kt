package com.buzzware.rapidouser.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.buzzware.rapidouser.R
import com.buzzware.rapidouser.utils.LocationUtility
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Locale

class AddressesDialogFragment(val addressClick: (String, String, String, LatLng) -> Unit) :
    DialogFragment(), OnMapReadyCallback {

    private var myGoogleMap: GoogleMap? = null
    private lateinit var autocompleteSupportFragment: AutocompleteSupportFragment
    private lateinit var locationUtility: LocationUtility
    val REQUEST_CODE = 1000
    private var userLat = 0.0
    private var userLng = 0.0

    private var fullAddress = ""
    private var city = ""
    private var postalCode = ""

    private lateinit var confirmBtn: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_addresses_dialog, container, false)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE
            )
        }
        confirmBtn = view.findViewById(R.id.confirm_btn)
        locationUtility = LocationUtility(requireContext())

        Places.initialize(requireContext(), getString(R.string.api_key))
        autocompleteSupportFragment =
            (childFragmentManager.findFragmentById(R.id.auto_complete_places) as AutocompleteSupportFragment)!!
        autocompleteSupportFragment.apply {
            setPlaceFields(
                listOf(
                    Place.Field.ID,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS_COMPONENTS
                )
            )
            setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onError(p0: Status) {
                    Toast.makeText(
                        requireContext(),
                        p0.statusMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("statusMsg", "onError: ${p0.statusMessage}")
                }

                override fun onPlaceSelected(place: Place) {
                    var city = ""
                    var postalCode = ""

                    place.addressComponents?.asList()?.forEach { component ->
                        when {
                            component.types.contains("locality") -> city = component.name
                            component.types.contains("postal_code") -> postalCode = component.name
                        }
                    }

                    val fullAddress = place.address ?: "Address not available"

                    myGoogleMap?.clear()
                    myGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17f))
                    myGoogleMap?.addMarker(MarkerOptions().position(place.latLng).title(fullAddress))

                    confirmBtn.setOnClickListener {
                        addressClick.invoke(fullAddress, city, postalCode, place.latLng)
                        dismiss()
                    }
                }
            })
        }

        location()
        return view
    }

    private fun location() {
        locationUtility.requestLocationUpdates { currentLocation ->
            userLat = currentLocation.latitude
            userLng = currentLocation.longitude
            if (isAdded) {
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
                mapFragment?.getMapAsync(this)
            }
            locationUtility.removeLocationUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onMapReady(googelMaps: GoogleMap) {
        myGoogleMap = googelMaps
        val latLng = LatLng(userLat, userLng)
        myGoogleMap?.setOnMapClickListener { latLng ->
            myGoogleMap?.clear()
            myGoogleMap?.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                Log.d("mapsLog", "onMapReady: ${addresses!!.get(0)}")
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    fullAddress = address.getAddressLine(0)
                    city = address.locality
                    postalCode = address.postalCode
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("mapsLog", "onMapReady: catch ${e.localizedMessage}")
            }
        }
        confirmBtn.setOnClickListener {
            addressClick.invoke(fullAddress, city, postalCode, latLng)
            Log.d("ADDRESS", "onPlaceSelected: $fullAddress,$city, $postalCode,$latLng")
            dismiss()
        }
        myGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
    }
}