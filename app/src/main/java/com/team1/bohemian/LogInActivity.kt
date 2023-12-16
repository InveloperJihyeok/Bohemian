package com.team1.bohemian

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.team1.bohemian.databinding.ActivityLoginBinding


class LogInActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null
    // Firebase Authentication 관리 클래스
    var auth = FirebaseAuth.getInstance()

    // GoogleLogin 관리 클래스
    var googleSignInClient: GoogleSignInClient ?= null

    //GoogleLogin
    val GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

        // 구글 로그인 클래스를 만듬
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 구글 로그인 버튼
        binding?.btnGoogleLogin?.setOnClickListener { googleLogin() }

        // 일반 로그인 버튼
        binding?.btnLogin?.setOnClickListener { regularLogin() }

        // 아이디 만들기
        binding?.btnSignup?.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) }
    }

    fun regularLogin(): Unit{
        var email = binding?.emailText?.text.toString()
        var password = binding?.pwText?.text.toString()
        if (email.isNullOrBlank() || email.isEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            if (password.isNullOrBlank() || password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                auth?.signInWithEmailAndPassword(
                    binding?.emailText?.text.toString(),
                    binding?.pwText?.text.toString()
                )?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 아이디 생성이 완료되었을 때
                        val user = FirebaseAuth.getInstance().addAuthStateListener{firebaseAuth ->
                            val sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                            with(sharedPref.edit()){
                                putString("uid",firebaseAuth.currentUser?.uid.toString())
                                apply()
                            }
                            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    } else {
                        // 아이디 생성이 실패했을 경우
                        Log.e("LogInActivity", "로그인 실패: ${task.exception?.message}")
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    fun googleLogin(){
        googleSignInClient?.signOut()?.addOnCompleteListener(this) {
            var signInIntent = googleSignInClient?.signInIntent
            signInIntent?.let { startActivityForResult(it, GOOGLE_LOGIN_CODE) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 구글에서 승인된 정보를 가지고 오기
        if (requestCode == GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result?.isSuccess == true) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Log.d("googlesignin", credential.toString())
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // 다음 페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {
                    Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Failed. ${task.exception}")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // 자동 로그인
        moveMainPage(auth?.currentUser)
    }

    fun moveMainPage(user: FirebaseUser?) {
        // 유저가 로그인함
        Log.d("Log in", "$user")
        if (user != null) {
            Toast.makeText(this, getString(R.string.login_complete), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}

