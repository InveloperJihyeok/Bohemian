package com.team1.bohemian

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.team1.bohemian.databinding.FragmentChatsBinding

class ChatsFragment: Fragment() {

    private lateinit var local_messages: ListView
    private lateinit var my_messages: ListView
//    private val userList_local = arrayListOf("파리지앵~", "에펠탑 나들이", "아침 조깅 하실 분")
//    private val userList_my = arrayListOf("바티칸 투어 동행", "티본스테이크 뿌술 분~", "포지타노 당일투어")
    private lateinit var localAdapter: ArrayAdapter<String>
    private lateinit var myAdapter: ArrayAdapter<String>
    private lateinit var database: FirebaseDatabase
    private lateinit var chatroomRef: DatabaseReference
    private lateinit var messagesRef: DatabaseReference
    private lateinit var binding: FragmentChatsBinding
    private val chatroomNames = mutableListOf<String>()

    override fun onResume(){
        super.onResume()
        loadExistingChatrooms()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(layoutInflater)
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        local_messages = view.findViewById(R.id.local_messages)
        my_messages = view.findViewById(R.id.my_messages)
        database = FirebaseDatabase.getInstance("https://bohemian-32f18-default-rtdb.asia-southeast1.firebasedatabase.app/")
        chatroomRef = database.reference.child("chatrooms")

        // Set up the list adapters
        localAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, chatroomNames)
        myAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, chatroomNames)
        local_messages.adapter = localAdapter
        my_messages.adapter = myAdapter

        local_messages.setOnItemClickListener { _, _, position, _ ->
            val selectedChatroom = chatroomNames[position]
            Toast.makeText(requireContext(), "Clicked on $selectedChatroom", Toast.LENGTH_SHORT).show()
            JoinChatroom(selectedChatroom)
        }

        my_messages.setOnItemClickListener { _, _, position, _ ->
            val selectedChatroom = chatroomNames[position]
            Toast.makeText(requireContext(), "Clicked on $selectedChatroom", Toast.LENGTH_SHORT).show()
            JoinChatroom(selectedChatroom)
        }

        val newChatroomButton: ImageButton = view.findViewById(R.id.newChatroomButton)
        newChatroomButton.setOnClickListener{
            createNewChatroom()
        }

        loadExistingChatrooms()

        return view
    }

    private fun JoinChatroom(selectedUser: String){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val chatroomId = chatroomRef.push().key
        val chatroom = Chatroom(
            roomId = chatroomId!!,
            roomName = "",
            members = listOf(userId!!, selectedUser)
        )

        chatroomRef.child(chatroomId).setValue(chatroom)

        if(selectedUser == "New Chatroom"){
            chatroomNames.add(chatroom.roomName)
            localAdapter.notifyDataSetChanged()
            myAdapter.notifyDataSetChanged()
        }

        val chatroomFragment = ChatroomFragment.newInstance(selectedUser)
        val transaction: FragmentTransaction =
            requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, chatroomFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun createNewChatroom(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Chatroom Name")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK"){ _, _ ->
            val chatroomName = input.text.toString().trim()
            if(chatroomName.isNotEmpty()){
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val chatroomId = chatroomRef.push().key
                val chatroom = Chatroom(
                    roomId = chatroomId!!,
                    roomName = chatroomName,
                    members = listOf(userId!!)
                )
                chatroomRef.child(chatroomId).setValue(chatroom)

                chatroomNames.add(chatroomName)
                localAdapter.notifyDataSetChanged()
                myAdapter.notifyDataSetChanged()

                val chatroomFragment = ChatroomFragment.newInstance(chatroomName)
                val transaction: FragmentTransaction =
                    requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, chatroomFragment)
                transaction.addToBackStack(null)
                transaction.commit()

//                chatroomFragment.updateUsername(chatroomName)
            }else{
                Toast.makeText(requireContext(), "Please enter a chatroom name", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel"){dialog, _ -> dialog.cancel()}

        builder.show()
    }

    private fun loadExistingChatrooms() {
        var userId = FirebaseAuth.getInstance().currentUser?.uid
        // Load existing chatroom names from the database and update chatroomNames list
        chatroomRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatroomNames.clear() // Clear existing chatroom names
                for (chatroomSnapshot in snapshot.children) {
                    val chatroom = chatroomSnapshot.getValue(Chatroom::class.java)
                    if (chatroom != null) {
                        if(userId in chatroom.members && chatroom.roomName.isNotEmpty()){
                            chatroomNames.add(chatroom.roomName)
                        }
                    }
                }
                localAdapter.notifyDataSetChanged()
                myAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter1 =
//            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList_local)
//        local_messages.adapter = adapter1
//        val adapter2 =
//            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList_my)
//        my_messages.adapter = adapter2
//
//        local_messages.setOnItemClickListener { _, _, position, _ ->
//            val selectedUser = userList_local[position]
//            Toast.makeText(requireContext(), "Clicked on $selectedUser", Toast.LENGTH_SHORT).show()
//
//            val chatroomFragment = ChatroomFragment.newInstance(selectedUser)
//            val transaction: FragmentTransaction =
//                requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, chatroomFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//
//        my_messages.setOnItemClickListener { _, _, position, _ ->
//            val selectedUser = userList_my[position]
//            Toast.makeText(requireContext(), "Clicked on $selectedUser", Toast.LENGTH_SHORT).show()
//
//            val chatroomFragment = ChatroomFragment.newInstance(selectedUser)
//            val transaction: FragmentTransaction =
//                requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragment_container, chatroomFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//    }
}