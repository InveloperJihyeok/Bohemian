package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.team1.bohemian.databinding.ItemMapRecyclerviewBinding
import com.team1.bohemian.R


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
            for (image in item.images!!) {
                val imageView = createImageView(image, imageContainer.resources)
                imageContainer.addView(imageView)
            }
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
        private fun createImageView(imageUrl: String, resources: Resources): ImageView {
            val imageView = ImageView(imageContainer.context)
            imageView.layoutParams = LinearLayout.LayoutParams(
                dpToPx(resources, 200), // 이미지 뷰의 너비 (예: 150dp)
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 0, 8, 0) // 이미지 간격을 조절할 수 있습니다.

            // Glide를 사용하여 이미지 로드 및 표시
            Glide.with(imageContainer)
                .load(imageView)
                .into(imageView)

            // ImageView에 클릭 리스너 추가
            imageView.setOnClickListener {
                showPopupWindow(imageContainer.context, imageUrl)
            }

            return imageView
        }
        @SuppressLint("MissingInflatedId")
        private fun showPopupWindow(context: Context, imageUrl: String) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_layout, null)

            // 팝업창 생성
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // 팝업창 안의 ImageView 설정
            val popupImageView: ImageView = popupView.findViewById(R.id.popupImageView)
            Glide.with(context)
                .load(imageUrl)
                .into(popupImageView)

            // 팝업창 표시 위치 설정 (예: 중앙)
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            // 팝업창 닫기
            popupView.findViewById<Button>(R.id.btn_exitPopUp).setOnClickListener{
                popupWindow.dismiss()
            }
        }

    }
}