package com.team1.bohemian

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.team1.bohemian.databinding.FragmentMyPageBinding
import com.team1.bohemian.databinding.FragmentSetProfileBinding

class MyPageFragment: Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private var binding: FragmentMyPageBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyPageBinding.inflate(layoutInflater)
        val view = binding?.root

        database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userRef = database.reference.child("users").child(userId!!)

        loadProfileData()

        binding?.btnLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Log Out", Toast.LENGTH_SHORT).show()
            // LogInActivity로 이동
            val intent = Intent(requireContext(), LogInActivity::class.java)
            startActivity(intent)
        }

        binding?.btnSetProfile?.setOnClickListener {
            val setProfileFragment = SetProfileFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, setProfileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return view
    }

    // 프로필 이미지 등록 필요함

    private fun loadProfileData() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nickname = snapshot.child("nickname").getValue<String>()
                    val birth = snapshot.child("age").getValue<String>()
                    val gender = snapshot.child("gender").getValue<String>()
                    //val subscriber = snapshot.child("subscriber number).getValue<String>()

                    binding?.textMyNickname?.setText(nickname)
                    binding?.textMyBirth?.text = birth
                    binding?.textMyGender?.text = gender
                    // binding?.textSubscriber?.text = subscriber
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 데이터 로드 실패 시 동작
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                Log.d("SetProfileFragment", "실패")
            }
        })
    }
}