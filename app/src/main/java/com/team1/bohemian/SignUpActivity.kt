package com.team1.bohemian

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.team1.bohemian.databinding.ActivitySignUpBinding
import org.w3c.dom.Text

class SignUpActivity: AppCompatActivity() {

    private var binding: ActivitySignUpBinding? = null
    // Firebase Authetication 관리 클래스
    var auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSignUpComplete?.setOnClickListener { signUpComplete() }
    }
    fun String?.isNullOrBlankOrEmpty(): Boolean {
        return this.isNullOrBlank() || this.isEmpty()
    }
    fun signUpComplete(): Unit {
        if (binding?.textSignUpID?.text.toString().isNullOrBlankOrEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            if (binding?.textSignUpPw?.text.toString().isNullOrBlankOrEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                if (binding?.textSignUpPw?.text.toString() == binding?.textSignUpPw2?.text.toString()) { // 일치
                    // 계정 생성
                    auth?.createUserWithEmailAndPassword(
                        binding?.textSignUpID?.text.toString(),
                        binding?.textSignUpPw?.text.toString()
                    )?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "아이디 생성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LogInActivity::class.java))
                            } else {
                                // 아이디 생성이 실패했을 경우
                                Log.e("SignUpActivity", "계정 생성 실패: ${task.exception?.message}")
                                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()

                            }
                        }
                } else { // 불일치
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}


