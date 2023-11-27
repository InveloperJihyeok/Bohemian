package com.team1.bohemian

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.team1.bohemian.databinding.FragmentMapBinding

class MapFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var city: String
    private lateinit var country: String
    private lateinit var reviewList: ArrayList<String>
    private lateinit var reviewDataList: ArrayList<MapItemData>

    private lateinit var mMap: GoogleMap
    var mapView: FragmentContainerView? = null

    private var binding: FragmentMapBinding? = null

    // bottomSheetBehavior
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    // Firebase
    private var storageReference = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com")
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var locationRef: DatabaseReference
    private lateinit var reviewRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(layoutInflater)
        val view = binding?.root

        // 구글 지도 표시
        mapView = view?.findViewById(R.id.mapContainer)
        mapView?.let {
            val mapFragment = childFragmentManager.findFragmentById(it.id) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        }

        // Bottom sheet 표시
        if (view != null) {
            initializePersistentBottomSheet(view)
            persistentBottomSheetEvent(view)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //위치 권한 체크
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            getCurrentLocation()
        }
        else{   //권한이 없을 때 권한 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
            getCurrentLocation()
        }
    }
    private fun getReviewData() {
        // 현재 위치 기반 리뷰 ID 탐색
        reviewList = ArrayList<String>()
        locationRef = database.reference.child("reviewByLocation").child(country).child(city)
        locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    // 각 child의 값을 가져와서 리스트에 추가
                    val item = childSnapshot.getValue<String>()
                    if (item != null) {
                        reviewList.add(item)
                    }
                }
                addReviewData()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("MapFragment", "리뷰 ID 추가 실패")
            }
        })
    }

    private fun addReviewData(){
        // 리뷰 ID 기반 리뷰 정보 조회
        reviewDataList = ArrayList<MapItemData>()
        reviewRef = database.reference.child("reviewsById")
        for (reviewId in reviewList) {
            val specificReviewRef = reviewRef.child(reviewId)
            specificReviewRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = snapshot.child("title").getValue<String>()
                    val tags = snapshot.child("tags").getValue<MutableList<String>>()
                    val images = snapshot.child("images").getValue<MutableList<String>>()
                    reviewDataList.add(MapItemData(title, tags, images))

                    // Firebase에서 가져온 리뷰 추가
                    val adapter = RecyclerUserAdapter(reviewDataList)
                    val mapRecycle = view?.findViewById<RecyclerView>(R.id.mapRecycle)
                    mapRecycle?.adapter = adapter
                    mapRecycle?.addItemDecoration(DistanceItemDecorator(10))
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("MapFragment", "리뷰 정보 추가 실패")
                }
            })
        }


    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location_ ->
                if (location_ != null) {
                    val address = getAddressFromLocation(location_.latitude, location_.longitude)
                    country = address?.countryName.toString()
                    city = address?.locality.toString()
                    Toast.makeText(requireContext(), "현재 위치는 $country $city", Toast.LENGTH_SHORT).show()
                    getReviewData()
                } else {
                    Log.d("error", "위치 정보를 가져올 수 없음")
                }
            }
    }
    private fun getAddressFromLocation(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(requireContext())
        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        return if (addresses!!.isNotEmpty()) addresses?.get(0) else null
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
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

    // Persistent BottomSheet 초기화
    private fun initializePersistentBottomSheet(view: CoordinatorLayout) {

        val bottomSheetLayout = view?.findViewById<LinearLayout>(R.id.bottom_sheet_layout)

        // BottomSheetBehavior에 layout 설정
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout!!)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                // BottomSheetBehavior state에 따른 이벤트
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Log.d("MainActivity", "state: hidden")
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("MainActivity", "state: expanded")
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("MainActivity", "state: collapsed")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Log.d("MainActivity", "state: dragging")
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("MainActivity", "state: settling")
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("MainActivity", "state: half expanded")
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

    }
    // PersistentBottomSheet 내부 버튼 click event
    private fun persistentBottomSheetEvent(view: CoordinatorLayout) {

        view?.findViewById<Button>(R.id.btn_expand)?.setOnClickListener {
            // BottomSheet의 최대 높이만큼 보여주기
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        }

    }

    class DistanceItemDecorator(private val divValue: Int, private val divColor: Int = Color.TRANSPARENT) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.top = divValue
            outRect.left = divValue
            outRect.bottom = divValue
            outRect.right = divValue
        }

    }
}