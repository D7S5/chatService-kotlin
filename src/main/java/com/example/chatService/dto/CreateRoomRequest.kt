package com.example.chatService.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRoomRequest (
        @field:NotBlank
        val name : String,

        @field:NotNull
        val type : RoomType,

        @field:Min(2)
        @field:Max(1000)
        val maxParticipants : Int
){
}