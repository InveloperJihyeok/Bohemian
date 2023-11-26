package com.team1.bohemian

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.team1.bohemian.R.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var mapView: FragmentContainerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapContainer)
        mapView?.let {
            val mapFragment = childFragmentManager.findFragmentById(it.id) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 시작 지점을 파리로 설정
        val paris = LatLng(48.8566, 2.3522)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(paris))

        // 지도 크기 조절
        val zoomLevel = 12.0f // 원하는 줌 레벨 조절
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, zoomLevel))

        // 마커 추가
        mMap.addMarker(
            MarkerOptions()
                .position(paris)
                .title("Paris")
        )
    }

}