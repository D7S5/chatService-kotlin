package com.example.chatService.converter

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

object TimeConvert {
    private val KST: ZoneId = ZoneId.of("Asia/Seoul")

    fun fromEpochMilliToKst(epochMillis: Long): OffsetDateTime =
        Instant.ofEpochMilli(epochMillis)
            .atZone(KST)
            .toOffsetDateTime()
}
