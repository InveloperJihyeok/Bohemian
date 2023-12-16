package com.team1.bohemian

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.team1.bohemian.databinding.FragmentReviewsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReviewsFragment: Fragment(){

    private lateinit var city: String
    private lateinit var country: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private val itemList = mutableListOf<ReviewData>()
    private val storageReference = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference
    private var realTimeDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private var dataPassListener: ReviewDetailListener ?= null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as? ReviewDetailListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        // 리뷰 작성 페이지로 이동
        view.findViewById<Button>(R.id.btn_writeReview).setOnClickListener{
            val reviewAddFragment = ReviewAddFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, reviewAddFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        recyclerView = view.findViewById(R.id.rvfragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReviewAdapter(itemList, this)
        recyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            getRoomDatabase()
        }

        return view
    }
    fun getRoomDatabase(){
        // Room Database
        val database = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "database-name")
            .fallbackToDestructiveMigration()
            .build()
        GlobalScope.launch(Dispatchers.IO) {
            val locationDataList = database.locationDataDao().getAllLocationData()
            val locationData = locationDataList.getOrNull(0)

            if (locationData != null) {
                // country 및 city 초기화
                country = locationData.country
                city = locationData.city
                Log.d("fatal", "$country $city")

                // Firebase Realtime Database에서 reviewId 리스트 가져오기
                getReviewId()
            }
        }
    }
    fun getReviewId(){
        val reviewsList = mutableListOf<String>()
        val reviewLocationReference = realTimeDatabase.reference.child("reviewByLocation").child(country).child(city)
        reviewLocationReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (reviewSnapshot in snapshot.children) {
                        val reviewId = reviewSnapshot.getValue(String::class.java)
                        if (reviewId != null) {
                            reviewsList.add(reviewId)
                        }
                    }
                    getReviewDetails(reviewsList)
                } else {Log.e("Review", "Data is not exist")}
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Review", "Error: ${error.message}")
            }
        })
    }
    fun getReviewDetails(reviewsList: MutableList<String>){
        Log.d("fatal", reviewsList.toString())
        for (reviewId in reviewsList){
            realTimeDatabase.reference.child("reviewsById").child(reviewId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val id = snapshot.child("id").getValue(String::class.java)
                        val uid = snapshot.child("uid").getValue(String::class.java)
                        val title = snapshot.child("title").getValue(String::class.java)
                        val tagsList = snapshot.child("tags").children.map { it.value as String }.toMutableList()

                        val reviewData = ReviewData(id,uid,title,"",tagsList)
                        addItem(reviewData)
                    } else {
                        Log.e("Review", "Data is not exist")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseDatabase", "Error: ${error.message}")
                }
            })
        }
    }

    private fun addItem(item: ReviewData) {
        if (!itemList.contains(item)) {
            itemList.add(item)
            adapter.notifyItemInserted(itemList.size - 1)
        }
    }
    fun filterReviews(tag: String){

    }
}
