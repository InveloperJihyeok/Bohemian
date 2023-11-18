package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
//        transaction.addToBackStack(null)
        transaction.commit()
    }

    lateinit var email: TextView
    lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.emailText)
        email.text = auth.currentUser?.email

        val bottomNavigation: NavigationBarView = findViewById(R.id.bottom_navigation_bar)
        bottomNavigation.selectedItemId = R.id.navigation_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_map -> {
                    replaceFragment(MapFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_reviews -> {
                    replaceFragment(ReviewsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_chats -> {
                    replaceFragment(ChatsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_my_page -> {
                    replaceFragment(MyPageFragment())
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

    }
}

