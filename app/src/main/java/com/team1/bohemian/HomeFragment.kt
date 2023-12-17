package com.team1.bohemian

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationListener
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class HomeFragment: Fragment(), ChatRoomListener {

    private lateinit var locationTextView: TextView
    private lateinit var weatherTextView: TextView
    private lateinit var locationListView: ListView
    private lateinit var geocoder: Geocoder
    private lateinit var locationManager: LocationManager
    private lateinit var apiKey: String
    private lateinit var database: FirebaseDatabase
    private lateinit var chatroomList: MutableList<Chatroom>
    private lateinit var chatIdList: MutableList<String>
    private lateinit var storageRef: StorageReference
    private lateinit var storyRef: DatabaseReference
    private lateinit var country_: String
    private lateinit var city_: String
    private lateinit var view: View

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_home, container, false) // fragment 는 false -> 화면 구성 이후 fragment 추가
        locationTextView = view.findViewById(R.id.locationTextView)
        weatherTextView = view.findViewById(R.id.weatherTextView)
        locationListView = view.findViewById(R.id.locationListView)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        apiKey = "a831ee3081bf2791a76306c0c8b222ae"

        // 리뷰 추가 버튼
        view.findViewById<Button>(R.id.btn_storyAdd).setOnClickListener{
            Log.d("fatal", "add button clicked")
            // Room Database
            val database = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "database-name")
                .fallbackToDestructiveMigration()
                .build()
            GlobalScope.launch(Dispatchers.IO) {
                val locationDataList = database.locationDataDao().getAllLocationData()
                val locationData = locationDataList.getOrNull(0)

                if (locationData != null) {
                    val intent = Intent(activity, StoryAddActivity::class.java)
                    intent.putExtra("country", locationData.country)
                    intent.putExtra("city", locationData.city)
                    startActivity(intent)
                }
            }

        }

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

        database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
        chatroomList = mutableListOf()
        fetchChatrooms()

        val adapter = object : ArrayAdapter<Chatroom> (
            requireContext(),
            android.R.layout.simple_list_item_1,
            chatroomList
            ){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View{
                val view = super.getView(position, convertView, parent)
                val chatroom = getItem(position)
                val chatroomName = chatroom?.chatRoomName?:""
                (view.findViewById(android.R.id.text1) as TextView).text = chatroomName
                return view
            }
        }
        locationListView.adapter = adapter

        //리스트뷰 아이템 클릭 이벤트
        locationListView.setOnItemClickListener{ _, _, position, _ ->
            Log.d("ITM",chatroomList.toString())
            val selectedChatroom = chatroomList[position]
            Toast.makeText(requireContext(), "Clicked on $selectedChatroom", Toast.LENGTH_SHORT).show()

            val chatRoomName = selectedChatroom.chatRoomName
            if(chatRoomName != null){
                fetchChatroomUid(chatRoomName) {chatRoomUid ->
                        val intent = Intent(requireContext(), MessageActivity::class.java)
                        intent.putExtra("chatRoomUid", chatRoomUid)
                        Log.d("ITM", "chatRoomUid: $chatRoomUid")
                        startActivity(intent)
                }
            }
            else{
//                Log.e("ITM", "ChatRoomName is null")
            }
        }

    }
    fun getStoryId(country: String, city: String, callback: (MutableMap<String, Map<String, String>>) -> Unit) {
        // 위치기반으로 story Id 가져오기
        storyRef = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference.child("storyByLocation").child(country).child(city)
        var storyList = mutableMapOf<String, Map<String, String>>()
        storyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (storySnapshot in snapshot.children) {
                        val storyId = storySnapshot.key
                        val uid = storySnapshot.child("uid").getValue(String::class.java)
                        val comment = storySnapshot.child("comment").getValue(String::class.java)
                        if (storyId != null && uid != null && comment != null) {
                            storyList[storyId] = mapOf("uid" to uid, "comment" to comment)
                        }
                    }
                } else {
                    Log.e("Review", "Data is not exist")
                }
                callback(storyList)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(storyList)
            }
        })
    }
    fun getImageUri(
        country: String,
        city: String,
        storyList: MutableMap<String, Map<String, String>>,
        callback: (MutableMap<String, MutableList<Uri>>) -> Unit
    ) {
        // 이미지 Uri 가져오기
        storageRef = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference.child("story").child(country).child(city)
        val storyImageList = mutableMapOf<String, MutableList<Uri>>()

        // 각 storyId에 대해 이미지 리스트를 비어 있는 MutableList로 초기화
        for (storyId in storyList.keys) {
            storyImageList[storyId] = mutableListOf()
        }

        // 각 storyId에 대한 이미지 리스트를 추가
        for (storyId in storyList.keys) {
            storageRef.child(storyId).listAll().addOnSuccessListener { result ->
                val downloadTasks = mutableListOf<Task<Uri>>()

                for (item in result.items) {
                    // 이미지 파일에 대한 다운로드 URL 얻기
                    val downloadTask = item.downloadUrl.addOnSuccessListener { uri ->
                        storyImageList[storyId]?.add(uri)
                    }
                    downloadTasks.add(downloadTask)
                }

                // 모든 다운로드 작업이 완료된 후에 콜백 호출
                Tasks.whenAllSuccess<Uri>(*downloadTasks.toTypedArray()).addOnSuccessListener {
                    // 모든 이미지 리스트가 추가된 storyImageList를 콜백으로 전달
                    callback(storyImageList)
                }
            }
        }
    }
    fun addStoryView(storyList: MutableMap<String, Map<String, String>>, storyImageList: MutableMap<String, MutableList<Uri>>){
        Log.d("fatal", "addStoryView")
        Log.d("fatal", storyList.toString())
        Log.d("fatal", storyImageList.toString())
        // 첫 이미지로 스토리뷰 생성
        val imageContainer = view.findViewById<LinearLayout>(R.id.storyImageContainer)
        for (storyId in storyImageList.keys){
            Log.d("fatal", "for storyId")
            val uri = storyImageList[storyId]?.get(0)

            val imageView = ImageView(imageContainer.context)
            val layoutParams = LinearLayout.LayoutParams(
                dpToPx(imageView.resources, 170), // 이미지 뷰의 너비 (예: 100픽셀)
                dpToPx(imageView.resources, 170)  // 이미지 뷰의 높이 (예: 100픽셀)
            )
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.FIT_START
            imageView.setPadding(10,0,10,0)
            Glide.with(imageView.context)
                .load(uri)
                .centerCrop()
                .into(imageView)

            // 이미지 뷰를 부모 레이아웃에 추가
            imageContainer.addView(imageView)

            // ImageView에 클릭 리스너 추가: 스토리 액티비티 열고 인터페이스로 정보 전달
            imageView.setOnClickListener {
                val uid = storyList[storyId]?.get("uid")
                val comment = storyList[storyId]?.get("comment")
                val imageList = storyImageList[storyId]

                // 이미지 URI를 String으로 변환
                val stringImageList = imageList?.map { it.toString() }?.toMutableList()

                // Room Database
                val database = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "database-name")
                    .fallbackToDestructiveMigration()
                    .build()
                GlobalScope.launch(Dispatchers.IO) {
                    val locationDataList = database.locationDataDao().getAllLocationData()
                    val locationData = locationDataList.getOrNull(0)

                    if (locationData != null) {
                        val intent = Intent(activity, StoryActivity::class.java)
                        intent.putExtra("country", locationData.country)
                        intent.putExtra("city", locationData.city)
                        intent.putExtra("uid", uid)
                        intent.putExtra("comment", comment)
                        intent.putStringArrayListExtra("imageList", ArrayList(stringImageList))
                        startActivity(intent)
                    }
                }
            }
        }
    }
    fun dpToPx(resources: Resources, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
    private fun fetchChatroomUid(chatRoomName: String, callback: (String?)-> Unit){
        val chatroomRef = database.reference.child("chatrooms")
        var query = chatroomRef.orderByChild("chatRoomName").equalTo(chatRoomName)
        Log.d("ITM", chatRoomName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val chatroom = data.getValue(Chatroom::class.java)
                    if (chatroom != null) {
                        val chatRoomUid = data.key
                        if(chatRoomUid != null){
                            Log.d("ITM", "uid: $chatRoomUid")
                            callback(chatRoomUid)
                            return
                        }
                    }
                }
                Log.e("ITM", "No $chatRoomName")
                callback(null) // No matching chatroom found
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ITM", "Failed to fetch chatroomUid: ${error.message}")
                callback(null)
            }
        })
    }


    private fun fetchChatrooms(){
        val chatroomRef = database.reference.child("chatrooms")
        chatroomRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatroomList.clear()
                for(data in snapshot.children){
                    val chatroom = data.getValue(Chatroom::class.java)
                    chatroom?.let{
                        chatroomList.add(it)
                    }
                }
                (locationListView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ITM", "Failed to fetch")
            }
        })
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
                getStoryId(country_, city_){ storyList ->
                    Log.d("fatal", "getStoryId: ${storyList.toString()}")
                    getImageUri(country_, city_, storyList){ storyImageList ->
                        Log.d("fatal", "getImageUri: ${storyImageList.toString()}")
                        addStoryView(storyList, storyImageList)
                    }
                }
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?){
        }
        override fun onProviderEnabled(provider: String) {
        }
        override fun onProviderDisabled(provider: String) {
        }
    }
    private fun updateLocationTextView(latitude: Double, longitude: Double): Boolean{
        try{
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
                ?: emptyList()

            if(addresses.isNotEmpty()) {
                val city = addresses[0].locality
                val country = addresses[0].countryName

                // 위치 저장
                country_ = country
                city_ = city

                val locationString = "$city, $country"
                locationTextView.text = "Current Location: $locationString"
            } else {
                locationTextView.text = "Current Location: Unknown"
            }
            return true
        }catch (e: Exception) {
            e.printStackTrace()
            return false
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

    override fun addChatRoomToList(chatRoomUid: String) {
        TODO("Not yet implemented")
    }

    override fun refreshChatRooms() {
        TODO("Not yet implemented")
    }
}