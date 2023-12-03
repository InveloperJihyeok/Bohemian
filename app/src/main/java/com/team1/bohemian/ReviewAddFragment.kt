package com.team1.bohemian

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ReviewAddFragment: Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_review_add, container, false)

        // 이미지 추가
        view.findViewById<ImageView>(R.id.addImage).setOnClickListener{
            Toast.makeText(requireContext(), "이미지 추가", Toast.LENGTH_SHORT)
            // TODO: 이미지 추가 코드
        }

        // 리뷰 입력 완료
        view.findViewById<Button>(R.id.btn_uploadReview).setOnClickListener{

        }

        // 리뷰 임시 저장
        view.findViewById<Button>(R.id.btn_saveReview).setOnClickListener{

        }

        return view
    }
}