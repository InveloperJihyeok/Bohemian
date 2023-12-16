package com.team1.bohemian

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.team1.bohemian.databinding.FragmentAddChatroomBinding

interface ChatRoomListener{
    fun addChatRoomToList(chatRoomUid: String)
    fun refreshChatRooms()
}
class AddChatroomFragment(private var chatRoomListener: ChatRoomListener) : Fragment() {
    private var binding: FragmentAddChatroomBinding?= null
    private val database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddChatroomBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnCreateChatRoom?.setOnClickListener{
            createChatRoom()
        }
    }

    private fun createChatRoom(){
        val chatRoomName = binding?.editTextChatRoomName?.text.toString().trim()

        if(chatRoomName.isNotEmpty()){
            val userId = Firebase.auth.currentUser?.uid
            val chatModel = ChatModel()
            chatModel.users[userId.toString()] = true
            chatModel.chatRoomName = chatRoomName

            val chatRoomRef = database.child("chatrooms").push()
            chatRoomRef.setValue(chatModel).addOnSuccessListener {
                val chatRoomUid = chatRoomRef.key
                Log.d("ITM", "REf: $chatRoomRef")
                Log.d("ITM", "Uid: $chatRoomUid")

                if(!chatRoomUid.isNullOrEmpty()){
                    chatRoomListener.addChatRoomToList(chatRoomUid)
                    chatRoomListener.refreshChatRooms()
                }

                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack()

                Log.d("ITM", "채팅방 생성")
            }.addOnFailureListener{
                Log.e("ITM", "채팅방 생성 실패")
            }
        }
    }
    override fun onDestroyView(){
        super.onDestroyView()
        binding = null
    }
}