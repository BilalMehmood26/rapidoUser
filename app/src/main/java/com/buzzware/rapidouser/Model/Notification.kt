package com.buzzware.rapidouser.Model

data class Notification(
    val description:String = "",
    val sender_Id:String = "",
    val title:String = "",
    val type:String = "",
    val userId:String = "",
    val time:Long = 0,
    val status:Boolean = false
)
