package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.media.Image
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class ReviewAdapter(private var itemList: MutableList<ReviewData>, private val reviewsFragment: ReviewsFragment) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_detail, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.userText.text = item.nickname
        holder.titleText.text = item.title
        // 리뷰 태그 추가
        holder.tagsContainer.removeAllViews() // 기존에 추가된 뷰를 모두 제거
        for (tag in item.tags!!) {
            val tagTextView = TextView(holder.itemView.context)
            tagTextView.text = tag
            tagTextView.setTypeface(null, Typeface.ITALIC)
            tagTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_dark))

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 16, 0) // 태그 간의 간격 조절, 필요에 따라 조절 가능
            tagTextView.layoutParams = layoutParams
            tagTextView.setOnClickListener {
                Toast.makeText(tagTextView.context, "Tag clicked", Toast.LENGTH_SHORT).show()
                val tagText = tagTextView.text.toString()
                reviewsFragment.filterReviews(tagText)
            }
            holder.tagsContainer.addView(tagTextView)
        }
        holder.imageContainer.removeAllViews()
        val storageRef = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference.child("reviews").child(item.id!!)
        storageRef.listAll().addOnSuccessListener { result ->
            // 성공적으로 목록을 가져왔을 때
            for (item in result.items) {
                // 이미지 파일에 대한 다운로드 URL 얻기
                item.downloadUrl.addOnSuccessListener { uri ->
                    // 다운로드 URL을 사용하여 이미지 뷰 동적으로 추가
                    val imageView = ImageView(holder.imageContainer.context)
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
                    holder.imageContainer.addView(imageView)

                    // ImageView에 클릭 리스너 추가
                    imageView.setOnClickListener {
                        showPopupWindow(holder.imageContainer.context, uri)
                    }
                }
            }
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
    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.user_name)
        val titleText: TextView = itemView.findViewById(R.id.textView_title)
        val tagsContainer: LinearLayout = itemView.findViewById(R.id.reviewTagsContainer)
        val imageContainer: LinearLayout = itemView.findViewById(R.id.reviewImageContainer)
        val followButton: Button = itemView.findViewById(R.id.btn_follow)
        val detailButton: Button = itemView.findViewById(R.id.btn_showDetails)
    }
}
