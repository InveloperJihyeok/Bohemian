package com.team1.bohemian

data class ChatMessage(
    val senderUid: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0
)
