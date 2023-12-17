package com.team1.bohemian

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.team1.bohemian.databinding.ActivityStoryAddBinding
import kotlin.random.Random

class StoryAddActivity: AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageContainer: LinearLayout
    private lateinit var imageView: ImageView
    private val imageList = arrayListOf<Uri>()
    private lateinit var Country: String
    private lateinit var City: String
    private lateinit var uid: String

    private lateinit var binding: ActivityStoryAddBinding
    private var databaseRef = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    private var storageRef = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference.child("story")
    private lateinit var userRef: DatabaseReference
    private lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryAddBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        Country = intent.getStringExtra("country")!!
        City = intent.getStringExtra("city")!!

        sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        uid = sharedPref.getString("uid", "Fail to load")!!
        userRef = databaseRef.child("users").child(uid!!)

        // 이미지 추가
        imageContainer = binding?.addStoryImageContainer!!
        binding?.btnAddStoryImage?.setOnClickListener{
            imageView = ImageView(imageContainer.context)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        // 스토리 추가
        binding?.btnAddStory?.setOnClickListener{
            // Story 정보 저장
            val storyId = generateRandomString(10)
            val comment = binding?.textStoryComment?.text.toString()
            val storyRef = databaseRef.child("storyByLocation").child(Country).child(City).child(storyId)
            storyRef.child("uid").setValue(uid)
            storyRef.child("comment").setValue(comment)
            userRef.child("story").push().setValue(storyId)

            // 스토리지에 저장
            val storageImageRef = storageRef.child(Country).child(City)

            for (imageUri in imageList) {
                storageImageRef.child(storyId).child("${System.currentTimeMillis()}.jpg").putFile(imageUri)
                Log.d("fatal", imageUri.toString())
            }
            Toast.makeText(this, "Story Added!", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // 선택한 이미지의 URI 가져오기
            val imageUri = data.data
            // 이미지 뷰에 설정
            imageView.setImageURI(imageUri)
            val layoutParams = LinearLayout.LayoutParams(
                dpToPx(imageView.resources, 250), // 이미지 뷰의 너비 (예: 100픽셀)
                dpToPx(imageView.resources, 250)  // 이미지 뷰의 높이 (예: 100픽셀)
            )
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(15,0,15,0)

            imageContainer.addView(imageView)
            imageList.add(imageUri!!)
        }
    }
    fun dpToPx(resources: Resources, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
    fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset[Random.nextInt(charset.length)] }
            .joinToString("")
    }
}