package com.example.chatService.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

@Converter(autoApply = true)
class OffsetDateTimeKstConverter : AttributeConverter<OffsetDateTime?, LocalDateTime?> {
    override fun convertToDatabaseColumn(attribute: OffsetDateTime?): LocalDateTime? {
        if (attribute == null) {
            return null
        }

        return attribute.atZoneSameInstant(KST_ZONE).toLocalDateTime()
    }

    override fun convertToEntityAttribute(dbData: LocalDateTime?): OffsetDateTime? {
        if (dbData == null) {
            return null
        }

        return dbData.atZone(KST_ZONE).toOffsetDateTime()
    }

    companion object {
        private val KST_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
