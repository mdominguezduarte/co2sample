package com.example.chemicalsamples.activities


import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.chemicalsamples.R
import com.example.chemicalsamples.models.SampleChemistry
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps_co2_samples.*


class MapsCo2Samples : AppCompatActivity(), OnMapReadyCallback {

    private var sampleChemistryDetails: SampleChemistry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_maps_co2_samples)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            sampleChemistryDetails =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SampleChemistry
        }

        if (sampleChemistryDetails != null) {

            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = sampleChemistryDetails!!.title

            toolbar_map.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {

        val position = LatLng(
            sampleChemistryDetails!!.latitude,
            sampleChemistryDetails!!.longitude
        )
        googleMap.addMarker(
            MarkerOptions().position(position).title(sampleChemistryDetails!!.location)
        )
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}
