package com.team1.bohemian

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import kotlin.random.Random

class ReviewAddFragment: Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageContainer: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var tagLayout: LinearLayout
    private val tagList = arrayListOf<String>()
    private val imageList = arrayListOf<Uri>()

    private var database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var userRef = database.reference.child("users").child(userId!!)
    private lateinit var reviewRef: DatabaseReference
    private val storageReference = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review_add, container, false)

        imageContainer = view.findViewById(R.id.reviewImageContainer)
        tagLayout = view.findViewById(R.id.tagLinearLayout)

        // 태그 추가
        view.findViewById<Button>(R.id.btn_addTag).setOnClickListener{
            val tag = view.findViewById<EditText>(R.id.editText_reviewTag)
            val tagText = TextView(tagLayout.context)
            tagText.text = "#${tag.text.toString()}"
            tag.text = null
            tagText.setTextColor(ContextCompat.getColor(tagText.context, android.R.color.holo_blue_light)) // 파란 글씨
            tagText.setTypeface(null, Typeface.ITALIC) // 이탤릭체 스타일
            tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            tagText.setPadding(5, 0, 5, 0)

            tagLayout.addView(tagText)
            tagList.add(tagText.text.toString())
        }
        // 이미지 추가
        view.findViewById<ImageView>(R.id.addImage).setOnClickListener{
            imageView = ImageView(imageContainer.context)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        // 리뷰 입력 완료
        view.findViewById<Button>(R.id.btn_uploadReview).setOnClickListener{
            fun generateRandomString(length: Int): String {
                val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                return (1..length)
                    .map { charset[Random.nextInt(charset.length)] }
                    .joinToString("")
            }
            val reviewCode = generateRandomString(10)
            reviewRef = userRef.child("reviews").child("${userId}-$reviewCode")

            val title = view.findViewById<EditText>(R.id.editText_reviewTitle).text.toString()
            val content = view.findViewById<EditText>(R.id.editText_reviewContent).text.toString()
            val tags = tagList
            val images = imageList

            if (title.isNotEmpty() && content.isNotEmpty() && tags.isNotEmpty() && images.isNotEmpty()){
                reviewRef.child("title").setValue(title)
                reviewRef.child("content").setValue(content)
                reviewRef.child("tags").setValue(tags)
                reviewRef.child("images").setValue(images)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }


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