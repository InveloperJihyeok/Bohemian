package com.team1.bohemian

import kotlin.collections.HashMap

class ChatModel (val users: HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap()){
    class Comment(val userId: String? = null, val message: String? = null, val time: String? = null)
}

