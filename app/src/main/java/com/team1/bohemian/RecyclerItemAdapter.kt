package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
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
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage


class RecyclerUserAdapter(private var items: ArrayList<MapItemData>) : RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerUserAdapter.ViewHolder, position: Int) {

        val item = items[position]
        val listener = View.OnClickListener { it ->
            Toast.makeText(it.context, "Clicked -> Title : ${item.title}", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.item_map_recyclerview, parent, false)
        return RecyclerUserAdapter.ViewHolder(inflatedView)
    }

    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val titleView: TextView = v.findViewById(R.id.item_map_title)
        private val buttonView: Button = v.findViewById(R.id.btn_itemView)
        private val tagsContainer: LinearLayout = v.findViewById(R.id.tagsContainer)
        private val imageContainer: LinearLayout = v.findViewById(R.id.itemPictures)

        // Firebase Storage Reference 생성
        private val storageReference = FirebaseStorage.getInstance("gs://bohemian-32f18.appspot.com").reference

        fun bind(listener: View.OnClickListener, item: MapItemData) {
            titleView.text = item.title
            buttonView.setOnClickListener(listener)

            // 기존에 있는 태그들을 제거
            tagsContainer.removeAllViews()
            // item.tags에 있는 각 태그에 대해 동적으로 TextView 생성 및 추가
            for (tag in item.tags!!) {
                val tagTextView = createTagTextView(tag)
                tagsContainer.addView(tagTextView)
            }

            // 기존에 있는 태그들을 제거
            imageContainer.removeAllViews()
            // item.tags에 있는 각 태그에 대해 동적으로 TextView 생성 및 추가
            createImageView(item.id!!)
        }
        private fun createTagTextView(tag: String): TextView {
            val tagTextView = TextView(tagsContainer.context)
            tagTextView.text = "#$tag"
            tagTextView.setTextColor(ContextCompat.getColor(tagsContainer.context, android.R.color.holo_blue_light)) // 파란 글씨
            tagTextView.setTypeface(null, Typeface.ITALIC) // 이탤릭체 스타일
            tagTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            tagTextView.setPadding(5, 5, 5, 5)

            // 클릭 리스너 추가
            tagTextView.setOnClickListener {
                // 클릭 시 수행할 동작 구현
                Toast.makeText(itemView.context, "Clicked tag: $tag", Toast.LENGTH_SHORT).show()
                // TODO: 리뷰 정보 읽고 지도에 마커 표시
            }

            return tagTextView
        }
        fun dpToPx(resources: Resources, dp: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
            ).toInt()
        }
        private fun createImageView(reviewId: String) {
            val reviewReference = storageReference.child("reviews").child("$reviewId")
            reviewReference.listAll()
                .addOnSuccessListener { result ->
                    // 성공적으로 목록을 가져왔을 때
                    for (item in result.items) {
                        // 이미지 파일에 대한 다운로드 URL 얻기
                        item.downloadUrl.addOnSuccessListener { uri ->
                            // 다운로드 URL을 사용하여 이미지 뷰 동적으로 추가
                            val imageView = ImageView(imageContainer.context)
                            val layoutParams = LinearLayout.LayoutParams(
                                dpToPx(imageView.resources, 130), // 이미지 뷰의 너비 (예: 100픽셀)
                                dpToPx(imageView.resources, 130)  // 이미지 뷰의 높이 (예: 100픽셀)
                            )
                            imageView.layoutParams = layoutParams
                            imageView.scaleType = ImageView.ScaleType.FIT_START
                            imageView.setPadding(10,0,10,0)
                            Glide.with(imageView.context)
                                .load(uri)
                                .centerCrop()
                                .into(imageView)

                            // 이미지 뷰를 부모 레이아웃에 추가
                            Log.d("MapFragment", "$uri")
                            imageContainer.addView(imageView)

                            // ImageView에 클릭 리스너 추가
                            imageView.setOnClickListener {
                                showPopupWindow(imageContainer.context, uri)
                            }
                        }.addOnFailureListener { exception ->
                            // 다운로드 URL 가져오기 실패 시
                            Log.e("MapFragment", "다운로드 URL 가져오기 실패: ${exception.message}")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // 목록 가져오기 실패 시
                    Log.e("MapFragment", "목록 가져오기 실패: ${exception.message}")
                }
        }
        @SuppressLint("MissingInflatedId")
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

    }
}