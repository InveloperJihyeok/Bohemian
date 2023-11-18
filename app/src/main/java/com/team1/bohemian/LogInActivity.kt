package com.team1.bohemian

import android.app.Activity
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
    var auth: FirebaseAuth ?= null

    // GoogleLogin 관리 클래스
    var googleSignInClient: GoogleSignInClient ?= null

    //GoogleLogin
    val GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase 로그인 통합 관리하는 오브젝트 만들기
        auth = FirebaseAuth.getInstance()

        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

        // 구글 로그인 클래스를 만듬
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 구글 로그인 버튼
        binding.btnGoogleLogin.setOnClickListener { googleLogin() }

        // 일반 로그인 버튼
        binding.btnLogin.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }

    fun googleLogin(){
        googleSignInClient?.signOut()?.addOnCompleteListener(this) {
            // Google 로그인 다시 시작
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
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    // 다음 페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
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
        } else {
            Toast.makeText(this, getString(R.string.automatic_login_fail), Toast.LENGTH_SHORT).show()
        }
    }

    // 회원 가입
    fun createAndLogin() {
//        auth?.createUserWithEmailAndPassword(email_)
    }
}

