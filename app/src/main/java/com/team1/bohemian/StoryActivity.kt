package com.team1.bohemian

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.team1.bohemian.databinding.ActivityStoryBinding

class StoryActivity: AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var binding: ActivityStoryBinding
    private var databaseRef = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Intent로부터 데이터 추출
        val country = intent.getStringExtra("country")
        val city = intent.getStringExtra("city")
        val uid = intent.getStringExtra("uid")
        val comment = intent.getStringExtra("comment")
        val imageList = intent.getStringArrayListExtra("imageList")

        // Nickname 설정
        databaseRef.child(uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (storySnapshot in snapshot.children) {
                        val nickname = snapshot.child("nickname").getValue<String>()
                        binding.textStoryNickname.text = nickname
                    }
                } else {
                    Log.e("Review", "Data is not exist")}
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        binding?.textStoryComment?.text = comment

        // ViewPager2 초기화
        viewPager = findViewById(R.id.viewPager2)
        val adapter = ImageViewPagerAdapter(imageList!!, supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        binding.btnStoryLike.setOnClickListener{
            Toast.makeText(this, "Like this post!", Toast.LENGTH_SHORT).show()
        }

        binding.btnStoryFollow.setOnClickListener{
            Toast.makeText(this, "Follow this user!", Toast.LENGTH_SHORT).show()
        }
    }
}