package com.team1.bohemian

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
//import com.team1.bohemian.ChatModel
import com.team1.bohemian.ChatModel.Comment
//import com.team1.bohemian.Friend
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.team1.bohemian.databinding.ActivityMessageBinding


class MessageActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    private var chatRoomUid : String? = null
    private var destinationUid : String? = null
    private var userId : String? = null
    private var recyclerView : RecyclerView? = null
    private var binding: ActivityMessageBinding ?= null
    private lateinit var imageView: ImageView
    private lateinit var textViewTopName: TextView

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        imageView = binding?.messageActivityImageView!!
        val editText = binding?.messageActivityEditText
        textViewTopName = binding?.messageActivityTextViewTopName!!

        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        userId = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        imageView.setOnClickListener {
            Log.d("ITM", "$destinationUid")
            val chatModel = ChatModel()
            chatModel.users.put(userId.toString(), true)
            chatModel.users.put(destinationUid!!, true)

            Log.d("ITM", "click")

            val messageText = editText?.text.toString().trim() // 입력된 텍스트에서 공백을 제거하고 가져옴
            if (messageText.isNotEmpty()) { // 메시지가 비어있지 않은 경우에만 전송
                val comment = Comment(userId, messageText, curTime)
                if (chatRoomUid == null) {
                    imageView.isEnabled = false
                    database.child("chatrooms").push().setValue(chatModel).addOnSuccessListener {
                        // 채팅방 생성
                        checkChatRoom()
                        // 메세지 보내기
                        Handler().postDelayed({
                            println(chatRoomUid)
                            database.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                            editText?.text = null
                        }, 1000L)
                        Log.d("chatUidNull dest", "$destinationUid")
                    }
                } else {
                    database.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                    editText?.text = null
                    Log.d("chatUidNotNull dest", "$destinationUid")
                }
            } else {
                // 메시지가 비어있을 때의 처리 (예: Toast 메시지 표시)
                Toast.makeText(this, "메시지를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        checkChatRoom()
    }

    private fun checkChatRoom(){
        database.child("chatrooms").orderByChild("users/$userId").equalTo(true)
                .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children){
                    println(item)
                    val chatModel = item.getValue<ChatModel>()
                    if(chatModel?.users!!.containsKey(destinationUid)){
                        chatRoomUid = item.key
                        imageView.isEnabled = true
                        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
                        recyclerView?.adapter = RecyclerViewAdapter()
                    }
                }
            }
        })
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {

        private val comments = ArrayList<Comment>()
        private var otheruser : OtherUser? = null
        init{
            database.child("users").child(destinationUid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    otheruser = snapshot.getValue<OtherUser>()
                    textViewTopName.text = otheruser?.name
                    getMessageList()
                }
            })
        }

        fun getMessageList(){
            database.child("chatrooms").child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<Comment>()
                        comments.add(item!!)
                        println(comments)
                    }
                    notifyDataSetChanged()
                    //메세지를 보낼 시 화면을 맨 밑으로 내림
                    recyclerView?.scrollToPosition(comments.size - 1)
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)

            return MessageViewHolder(view)
        }
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if(comments[position].userId.equals(userId)){ // 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            }else{ // 상대방 채팅
                Glide.with(holder.itemView.context)
                        .load(otheruser?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.imageView_profile)
                holder.textView_name.text = otheruser?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}