package com.example.chatService.config

import com.example.chatService.dto.GroupMessageDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class GroupKafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean(name = ["groupConsumerFactory"])
    fun groupConsumerFactory(): ConsumerFactory<String, GroupMessageDto> {
        val props = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "group-chat-service",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        val valueDeserializer = JsonDeserializer(GroupMessageDto::class.java).apply {
            addTrustedPackages("com.example.chatService.dto")
            setUseTypeHeaders(false)
        }

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), valueDeserializer)
    }

    @Bean(name = ["groupKafkaListenerContainerFactory"])
    fun groupKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, GroupMessageDto> {
        return ConcurrentKafkaListenerContainerFactory<String, GroupMessageDto>().apply {
            consumerFactory = groupConsumerFactory()
        }
    }
}
