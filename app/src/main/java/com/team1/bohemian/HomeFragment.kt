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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class HomeFragment: Fragment() {

    private lateinit var locationTextView: TextView
    private lateinit var weatherTextView: TextView
    private lateinit var locationListView: ListView
    private val userList = arrayListOf("맛집 투어 갈 동행 구해요~", "카페 갈 사람ㄱ", "서로 인생샷 건져주실 분")    //가짜 사용자 데이터 -> 이거를 위치기반 근처 사람들 톡방으로 만들어야함
    private lateinit var geocoder: Geocoder
    private lateinit var locationManager: LocationManager
    private lateinit var apiKey: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false) // fragment 는 false -> 화면 구성 이후 fragment 추가
        locationTextView = view.findViewById(R.id.locationTextView)
        weatherTextView = view.findViewById(R.id.weatherTextView)
        locationListView = view.findViewById(R.id.locationListView)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        apiKey = "a831ee3081bf2791a76306c0c8b222ae"
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

            //ChatroomFragment로 이동
            val chatroomFragment = ChatroomFragment.newInstance(selectedUser)
            val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, chatroomFragment)
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

            GlobalScope.launch(Dispatchers.Main){
                getWeatherInfo(location.latitude, location.longitude)
            }
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
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    data class WeatherResponse(
        val weather: List<WeatherInfo>,
        val main: MainInfo
    )
    data class WeatherInfo(
        val main: String,
        val description: String
    )
    data class MainInfo(
        val temp: Double
    )
    interface WeatherApi{
        @GET("weather")
        suspend fun getWeather(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): WeatherResponse
    }
    private suspend fun getWeatherInfo(latitude: Double, longitude: Double){
        try{
            val weather = fetchWeatherData(latitude, longitude)
            weatherTextView.text = "Weather: $weather"
        }catch (e: Exception){
            e.printStackTrace()
            weatherTextView.text = "Weather: Unknown"
        }
    }

    private fun createWeatherApi(): WeatherApi{
        val retrofit = Retrofit.Builder()
            .baseUrl(Companion.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(WeatherApi::class.java)
    }
    private suspend fun fetchWeatherData(latitude: Double, longitude: Double): String{
        try{
            val weatherApi = createWeatherApi()
            val response = weatherApi.getWeather(latitude, longitude, apiKey)

            val weatherDescription = response.weather.firstOrNull()?.description?:"Unknown"
            val temperature = response.main.temp.toString()

            return "$weatherDescription, Temperature: $temperature°C"
        }catch (e: Exception){
            e.printStackTrace()
            return "Weather: Unknown"
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }
}