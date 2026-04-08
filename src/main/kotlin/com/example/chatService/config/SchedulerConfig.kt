package com.example.chatService.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Configuration
@EnableScheduling
class SchedulerConfig : SchedulingConfigurer {
    override fun configureTasks(registrar: ScheduledTaskRegistrar) {
        val scheduler = ThreadPoolTaskScheduler().apply {
            poolSize = 5
            initialize()
        }
        registrar.setTaskScheduler(scheduler)
    }
}
