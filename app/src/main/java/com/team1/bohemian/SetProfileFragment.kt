package com.team1.bohemian

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.team1.bohemian.databinding.FragmentSetProfileBinding
import java.util.Calendar

class SetProfileFragment: Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private var binding: FragmentSetProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSetProfileBinding.inflate(layoutInflater)
        val view = binding?.root

        database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userRef = database.reference.child("users").child(userId!!)

        // 데이터 로드 및 설정
        loadProfileData()

        binding?.btnEnterProfile?.setOnClickListener {
            saveProfileData()
        }

        // 생년월일 선택 버튼 클릭 시 DatePickerDialog 표시
        binding?.btnSelectBirthdate?.setOnClickListener {
            showDatePickerDialog()
        }

        return view
    }

    // Firebase에서 데이터 로드하여 설정
    private fun loadProfileData() {
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nickname = snapshot.child("nickname").getValue<String>()
                    val age = snapshot.child("age").getValue<String>()
                    val gender = snapshot.child("gender").getValue<String>()

                    binding?.editTextNickname?.setText(nickname)
                    binding?.textBirth?.text = age

                    if (gender == "Male") {
                        binding?.radioMale?.isChecked = true
                    } else if (gender == "Female") {
                        binding?.radioFemale?.isChecked = true
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // 데이터 로드 실패 시 동작
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                Log.d("SetProfileFragment", "실패")
            }
        })
    }

    // Firebase에 데이터 저장
    private fun saveProfileData() {
        val nickname = binding?.editTextNickname?.text.toString().trim()
        val birth = binding?.textBirth?.text.toString()
        val gender = when (binding?.radioGroupGender?.checkedRadioButtonId) {
            R.id.radioMale -> "Male"
            R.id.radioFemale -> "Female"
            else -> ""
        }

        if (nickname.isNotEmpty() && birth.isNotEmpty() && gender.isNotEmpty()) {
            userRef.child("nickname").setValue(nickname)
            userRef.child("age").setValue(birth)
            userRef.child("gender").setValue(gender)

            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            Log.d("SetProfileFragment", "Profile updated")
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    // DatePickerDialog 표시
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // 생년월일 선택 완료 시 동작
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding?.textBirth?.text = selectedDate
            },
            year,
            month,
            dayOfMonth
        )
        datePickerDialog.show()
    }
}