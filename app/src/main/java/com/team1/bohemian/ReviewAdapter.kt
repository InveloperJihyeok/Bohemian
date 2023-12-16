package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.ClipData
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

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

        //holder.imagePager

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userText: TextView = itemView.findViewById(R.id.user_name)
        val titleText: TextView = itemView.findViewById(R.id.textView_title)
        val tagsContainer: LinearLayout = itemView.findViewById(R.id.reviewTagsContainer)
        val imagePager: ViewPager2 = itemView.findViewById(R.id.reviewViewPager)
        val followButton: Button = itemView.findViewById(R.id.btn_follow)
        val detailButton: Button = itemView.findViewById(R.id.btn_showDetails)
    }
}
