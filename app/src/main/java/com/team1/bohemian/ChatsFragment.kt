package com.team1.bohemian

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class ChatsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)

        arguments?.let{
            val username = it.getString(USERNAME_KEY)
            username?.let{
                usernameTextView.text = it
            }
        }
        return view
    }

    companion object {
        const val USERNAME_KEY = "username"

        fun newInstance(username: String): ChatsFragment {
            val fragment = ChatsFragment()
            val bundle = Bundle()
            bundle.putString(USERNAME_KEY, username)
            fragment.arguments = bundle
            return fragment
        }
    }
}