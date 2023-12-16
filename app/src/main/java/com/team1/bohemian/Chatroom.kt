package com.team1.bohemian

data class Chatroom(
    val chatRoomUid: String? = null,
    val chatRoomName: String? = null,
    val users: Map<String, Boolean> ?= null)
