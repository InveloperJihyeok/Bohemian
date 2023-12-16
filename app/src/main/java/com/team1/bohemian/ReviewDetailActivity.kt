package com.team1.bohemian

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.team1.bohemian.databinding.ActivityMainBinding
import com.team1.bohemian.databinding.ActivityReviewDetailBinding

class ReviewDetailActivity: AppCompatActivity(){
    private var binding: ActivityReviewDetailBinding ?= null
    private var reviewRef = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("reviewsById")
    private val storageRef = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference.child("reviews")
    private var userRef = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("users")
    private lateinit var reviewId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        reviewId = intent.getStringExtra("reviewId")!!
        reviewRef.child(reviewId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val id = snapshot.child("id").getValue<String>()
                    val uid = snapshot.child("uid").getValue<String>()
                    val title = snapshot.child("title").getValue<String>()
                    val content = snapshot.child("content").getValue<String>()
                    val tagsList = snapshot.child("tags").children.map { it.value as String }.toMutableList()
                    val imageContainer = binding?.reviewDetailImageContainer
                    storageRef.child(id!!).listAll().addOnSuccessListener { result ->
                        // 성공적으로 목록을 가져왔을 때
                        for (item in result.items) {
                            // 이미지 파일에 대한 다운로드 URL 얻기
                            item.downloadUrl.addOnSuccessListener { uri ->
                                // 다운로드 URL을 사용하여 이미지 뷰 동적으로 추가
                                val imageView = ImageView(imageContainer?.context)
                                val layoutParams = LinearLayout.LayoutParams(
                                    dpToPx(imageView.resources, 300),
                                    dpToPx(imageView.resources, 300)
                                )
                                imageView.layoutParams = layoutParams
                                imageView.scaleType = ImageView.ScaleType.FIT_START
                                imageView.setPadding(10,0,10,0)
                                Glide.with(imageView.context)
                                    .load(uri)
                                    .centerCrop()
                                    .into(imageView)

                                // 이미지 뷰를 부모 레이아웃에 추가
                                imageContainer?.addView(imageView)

                                imageView.setOnClickListener {
                                    showPopupWindow(imageContainer!!.context, uri)
                                }
                            }
                        }
                    }
                    binding?.reviewDetailTitle?.text = title
                    userRef.child(uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("fatal", "onDataChange")
                            if (snapshot.exists()) {
                                val nickname = snapshot.child("nickname").getValue<String>()
                                binding?.reviewDetailNickname?.text = nickname
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                    binding?.reviewDetailContent?.text = content
                }
            }
            private fun showPopupWindow(context: Context, imageUrl: Uri) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = inflater.inflate(R.layout.popup_layout, null)

                // 팝업창 안의 ImageView 설정
                val popupImageView: ImageView = popupView.findViewById(R.id.popupImageView)
//            popupImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(context)
                    .load(imageUrl)
                    .into(popupImageView)

                // 팝업창 생성
                val popupWindow = PopupWindow(
                    popupView,
                    dpToPx(popupImageView.resources, 350), // 가로 크기 (예: 100dp)
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                )

                // 팝업창 표시 위치 설정 (예: 중앙)
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

                // 팝업창 닫기
                popupView.findViewById<Button>(R.id.btn_exitPopUp).setOnClickListener{
                    popupWindow.dismiss()
                }
            }
            fun dpToPx(resources: Resources, dp: Int): Int {
                return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
                ).toInt()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("fatal", "Error: ${error.message}")
            }
        })

        binding?.reviewDetailExitButton?.setOnClickListener{
            finish()
        }
        binding?.reviewDetailFollowButton?.setOnClickListener{
            Toast.makeText(this, "Follow!", Toast.LENGTH_SHORT).show()
        }

    }
}