package com.team1.bohemian

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import kotlin.random.Random

class ReviewAddFragment: Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageContainer: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var tagLayout: LinearLayout
    private val tagList = arrayListOf<String>()
    private val imageList = arrayListOf<Uri>()
    private lateinit var userId: String

    private var database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var userRef: DatabaseReference
    private lateinit var reviewRef: DatabaseReference
    private lateinit var sharedPref: SharedPreferences
    private val storageRef = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference.child("reviews")

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review_add, container, false)

        imageContainer = view.findViewById(R.id.reviewImageContainer)
        tagLayout = view.findViewById(R.id.tagLinearLayout)

        sharedPref = requireActivity().getSharedPreferences("userInfo",Context.MODE_PRIVATE)
        userId = sharedPref.getString("uid","")!!
        userRef = database.reference.child("users").child(userId!!)

        // 태그 추가
        view.findViewById<Button>(R.id.btn_addTag).setOnClickListener{
            val tag = view.findViewById<EditText>(R.id.editText_reviewTag)
            val tagText = TextView(tagLayout.context)
            val tagtext = tag.text.toString()
            tagText.text = "#$tagtext"
            tag.text = null
            tagText.setTextColor(ContextCompat.getColor(tagText.context, android.R.color.holo_blue_light)) // 파란 글씨
            tagText.setTypeface(null, Typeface.ITALIC) // 이탤릭체 스타일
            tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            tagText.setPadding(5, 0, 5, 0)

            tagLayout.addView(tagText)
            tagList.add(tagtext)
        }
        // 이미지 추가
        view.findViewById<ImageView>(R.id.addImage).setOnClickListener{
            imageView = ImageView(imageContainer.context)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        fun generateRandomString(length: Int): String {
            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { charset[Random.nextInt(charset.length)] }
                .joinToString("")
        }
        // 리뷰 입력 완료
        view.findViewById<Button>(R.id.btn_uploadReview).setOnClickListener{
            // 리뷰 아이디 생성
            val reviewCode = generateRandomString(10)

            // 입력된 리뷰 정보 변수로 정의
            val title = view.findViewById<EditText>(R.id.editText_reviewTitle).text.toString()
            val content = view.findViewById<EditText>(R.id.editText_reviewContent).text.toString()
            val tags = tagList

            if (title.isNotEmpty() && content.isNotEmpty() && tags.isNotEmpty()){
                // ReviewsById에 리뷰 정보 저장
                val reviewsByIdRef = database.reference.child("reviewsById").child(reviewCode)
                reviewsByIdRef.child("id").setValue(reviewCode)
                reviewsByIdRef.child("uid").setValue(userId)
                reviewsByIdRef.child("title").setValue(title)
                reviewsByIdRef.child("content").setValue(content)
                reviewsByIdRef.child("tags").setValue(tags)

                // Firebase Storage에 이미지 추가
                for (imageUri in imageList){
                    val reviewStorageRef = storageRef.child(reviewCode).child("${System.currentTimeMillis()}.jpg")
                    val uploadTask = reviewStorageRef.putFile(imageUri)
                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            Log.d("reviews", downloadUrl)
                        }
                    }.addOnFailureListener {
                        Log.d("reviews", "Storage 업로드 실패")
                    }
                }

                // users.userId.reviews에 리뷰 아이디 추가
                userRef.child("reviews").push().setValue(reviewCode)

                // ReviewsByLocation에 리뷰 아이디 추가
                var country: String
                var city: String
                val roomDatabase = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "database-name")
                    .fallbackToDestructiveMigration()
                    .build()
                GlobalScope.launch(Dispatchers.IO){
                    val savedData = roomDatabase.locationDataDao().getAllLocationData().get(0)
                    country = savedData.country
                    city = savedData.city
                    database.reference.child("reviewByLocation").child(country).child(city).push().setValue(reviewCode)
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 리뷰 임시 저장
        view.findViewById<Button>(R.id.btn_saveReview).setOnClickListener{

        }

        return view
    }
    fun dpToPx(resources: Resources, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // 선택한 이미지의 URI 가져오기
            val imageUri = data.data
            // 이미지 뷰에 설정
            imageView.setImageURI(imageUri)
            val layoutParams = LinearLayout.LayoutParams(
                dpToPx(imageView.resources, 102), // 이미지 뷰의 너비 (예: 100픽셀)
                dpToPx(imageView.resources, 102)  // 이미지 뷰의 높이 (예: 100픽셀)
            )
            imageView.layoutParams = layoutParams
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(10,0,10,0)

            imageContainer.addView(imageView)
            imageList.add(imageUri!!)
        }
    }
}