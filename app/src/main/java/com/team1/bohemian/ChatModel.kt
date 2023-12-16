package com.team1.bohemian

import kotlin.collections.HashMap

class ChatModel (val users: HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap(),
                 var chatRoomName: String? = null){

    class Comment(val userId: String? = null,
                  val message: String? = null,
                  val time: String? = null)

    fun addUser(userId: String){
        users[userId] = true
    }

    fun addComment(commentId: String, comment: Comment) {
        comments[commentId] = comment
    }

    fun containsUser(userId: String?): Boolean {
        return users.containsKey(userId) && users[userId] == true
    }

}

