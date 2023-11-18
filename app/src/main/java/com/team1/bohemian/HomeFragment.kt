package com.team1.bohemian

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import java.util.*

class HomeFragment: Fragment() {

    private lateinit var locationTextView: TextView
    private lateinit var locationListView: ListView
    private val userList = arrayListOf("User 1", "User 2", "User 3")    //가짜 사용자 데이터 -> 이거를 위치기반 근처 사람들 톡방으로 만들어야함
    private lateinit var geocoder: Geocoder

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false) // fragment 는 false -> 화면 구성 이후 fragment 추가
        locationTextView = view.findViewById(R.id.locationTextView)
        locationListView = view.findViewById(R.id.locationListView)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //위치 권한 체크
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ){
            getLocation()   //위치 정보 얻어오기
        }
        else{   //권한이 없을 때 권한 요청
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        //가짜 사용자 데이터로 리스트뷰 구성
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList)
        locationListView.adapter = adapter

        //리스트뷰 아이템 클릭 이벤트
        locationListView.setOnItemClickListener{ _, _, position, _ ->
            val selectedUser = userList[position]
            Toast.makeText(requireContext(), "Clicked on $selectedUser", Toast.LENGTH_SHORT).show()

            //chatFragment로 이동
            val chatsFragment = ChatsFragment.newInstance(selectedUser)
            val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, chatsFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val minTime: Long = 10000   //최소 시간 간격(msec)
        val minDistance = 10.0f     //최소 거리 간격(meter)

        //위치 업데이트 요청
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            minTime,
            minDistance,
            locationListener
        )
    }

    private val locationListener = object : LocationListener{
        override fun onLocationChanged(location: Location){
            //위치가 변경되었을 때 실행되는 코드
            //여기에서 서버에 위치 정보를 업로드하거나, 주변 사용자를 가져오는 등의 작업 수행
            updateLocationTextView(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){
        }
        override fun onProviderEnabled(provider: String) {
        }
        override fun onProviderDisabled(provider: String) {
        }
    }
    private fun updateLocationTextView(latitude: Double, longitude: Double){
        try{
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
                ?: emptyList()

            if(addresses.isNotEmpty()) {
                val city = addresses[0].locality
                val country = addresses[0].countryName

                val locationString = "$city, $country"
                locationTextView.text = "Current Location: $locationString"
            } else {
                locationTextView.text = "Current Location: Unknown"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}