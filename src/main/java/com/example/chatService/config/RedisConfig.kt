package com.example.chatService.config

import com.example.chatService.component.RedisKeyExpiredListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.ConfigureRedisAction

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
    }

    @Bean
    fun redisContainer(
        connectionFactory: RedisConnectionFactory,
        listener: RedisKeyExpiredListener
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            addMessageListener(listener, PatternTopic("__keyevent@*__:expired"))
        }
    }

    companion object {
        @Bean
        @JvmStatic
        fun configureRedisAction(): ConfigureRedisAction = ConfigureRedisAction.NO_OP
    }
}
