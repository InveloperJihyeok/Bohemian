package com.team1.bohemian

data class Chatroom(
    val roomId: String = "",
    val roomName: String = "",
    val members: List<String> = emptyList()
)
