package com.example.chatService.config

import com.example.chatService.dto.DMMessageKafkaDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, DMMessageKafkaDto> {
        val deserializer = JsonDeserializer(DMMessageKafkaDto::class.java).apply {
            addTrustedPackages("com.example.chatService.dto")
        }

        return DefaultKafkaConsumerFactory(
            mapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "chat_service",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
            ),
            StringDeserializer(),
            deserializer
        )
    }

    @Bean(name = ["DMKafkaListenerContainerFactory"])
    fun dmKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, DMMessageKafkaDto> {
        return ConcurrentKafkaListenerContainerFactory<String, DMMessageKafkaDto>().apply {
            consumerFactory = kafkaConsumerFactory()
        }
    }
}