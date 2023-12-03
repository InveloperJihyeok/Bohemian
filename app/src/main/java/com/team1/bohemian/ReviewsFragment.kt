package com.team1.bohemian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ReviewsFragment: Fragment() {
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

        return view
    }
}