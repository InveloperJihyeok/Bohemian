package com.team1.bohemian

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.Collections.reverseOrder
import kotlin.collections.ArrayList

class ChatFragment : Fragment(), ChatRoomListener{

    private val fireDatabase = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.chatfragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()
        val addChatRoomButton = view.findViewById<ImageButton>(R.id.addChatRoomButton)
        addChatRoomButton.setOnClickListener{
            showAddChatRoomFragment()
        }

        return view
    }
    private fun showAddChatRoomFragment(){
        val addChatroomFragment = AddChatroomFragment(this@ChatFragment)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addChatroomFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun addChatRoomToList(chatRoomUid: String) {
        moveToChatRoom(chatRoomUid)
    }

    override fun refreshChatRooms() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.chatfragment_recyclerview)
        recyclerView?.adapter?.notifyDataSetChanged()
    }
    private fun moveToChatRoom(chatRoomUid: String?){
        if(chatRoomUid != null){
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("chatRoomUid", chatRoomUid)
            startActivity(intent)
        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        private val chatModel = ArrayList<ChatModel>()
        private var userId : String? = null
        private val destinationUsers : ArrayList<String> = arrayListOf()

        init {
            userId = Firebase.auth.currentUser?.uid.toString()
            Log.d("ITM", "id:$userId")
            fireDatabase.child("chatrooms").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatModel.clear()
                    for(data in snapshot.children){
                        val chatModelItem = data.getValue<ChatModel>()
                        chatModelItem?.let {
                            if(it.users.containsKey(userId)){
                                chatModel.add(it)
                                Log.d("ITM", "chatModel:$chatModel")
                                Log.d("ChatFragment", "ChatRoomName: ${it.chatRoomName}")

                                val chatRoomUid = data.key

                                if (chatRoomUid != null) {
                                    destinationUsers.add(chatRoomUid)
                                }
                            }
                        }
                    }
                    notifyDataSetChanged()
                }
            })
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.chat_item_imageview)
            val textView_title : TextView = itemView.findViewById(R.id.chat_textview_title)
            val textView_lastMessage : TextView = itemView.findViewById(R.id.chat_item_textview_lastmessage)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, @SuppressLint("RecyclerView") position: Int) {
            //채팅방에 있는 유저 모두 체크
            val users = chatModel[position].users.keys.toList()
            val otherUser = users.firstOrNull{it != userId}

            var chatRoomUid: String? = null

            if (otherUser != null) {
                destinationUsers.add(otherUser)
                chatRoomUid = otherUser
                Log.d("ITM", "Des: $destinationUsers")
            }

            fireDatabase.child("users").child("$chatRoomUid").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friend = snapshot.getValue<OtherUser>()
                    Glide.with(holder.itemView.context).load(friend?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.imageView)
                    holder.textView_title.text = friend?.name
                    holder.textView_title.text = "${chatModel[position].chatRoomName}"
                }
            })
            //메세지 내림차순 정렬 후 마지막 메세지의 키값을 가져옴
            val commentMap = TreeMap<String, ChatModel.Comment>(reverseOrder())
            commentMap.putAll(chatModel[position].comments)
            if(commentMap.isNotEmpty()){
                val lastMessageKey = commentMap.keys.toTypedArray()[0]
                if(chatModel[position].comments.containsKey(lastMessageKey)){
                    holder.textView_lastMessage.text = chatModel[position].comments[lastMessageKey]?.message
                }
                else{
                    holder.textView_lastMessage.text = ""
                }
            }else{
                holder.textView_lastMessage.text = ""
            }


            //채팅창 선책 시 이동
            holder.itemView.setOnClickListener {
                Log.d("ITM", "click")
                Log.d("ITM", "$chatRoomUid")
                if(position < destinationUsers.size){
                    val intent = Intent(context, MessageActivity::class.java)
                    intent.putExtra("chatRoomUid", destinationUsers[position])
                    context?.startActivity(intent)
                }
            }
        }
        override fun getItemCount(): Int {
            return chatModel.size
        }
    }
}
