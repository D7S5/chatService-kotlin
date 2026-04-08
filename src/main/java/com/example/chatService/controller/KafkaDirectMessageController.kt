package com.example.chatService.controller

import com.example.chatService.dto.DMMessageKafkaDto
import com.example.chatService.kafka.DMProducerService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class KafkaDirectMessageController(
    private val dmProducerService: DMProducerService,
) {
    @MessageMapping("/dm.send")
    fun send(dto: DMMessageKafkaDto) {
        dmProducerService.publish(dto)
    }
}
