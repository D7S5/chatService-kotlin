package com.example.chatService.dto

data class DMMessageKafkaDto (
        val roomId : String,
        val senderId : String,
        val content : String,
        val sentAt : Long
){
}