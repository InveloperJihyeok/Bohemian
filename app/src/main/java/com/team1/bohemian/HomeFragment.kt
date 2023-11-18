package com.team1.bohemian

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false) // fragment 는 false -> 화면 구성 이후 fragment 추가
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<Button>(R.id.btn_enter_product_detail)
        button.setOnClickListener {
            Log.d("check", "button clicked")
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, ProductDetailFragment())
            transaction.addToBackStack(null) // 이전 상태를 백 스택에 저장
            transaction.commit()
        }
    }
}