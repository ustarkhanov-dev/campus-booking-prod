package ru.bgpu.campus.booking.mapper

import ru.bgpu.campus.booking.dto.RecordResponse
import ru.bgpu.campus.booking.models.Records

object RecordMapper {

    fun toResponse(record: Records): RecordResponse {
        return RecordResponse(
            id = record.id,
            bookingDate = record.bookingDate,
            recordDate = record.recordDate,
            username = record.user?.username ?: "Неизвестный пользователь",
            resourceName = record.resource?.name ?: "Неизвестный ресурс",
            status = record.status,
            startTime = record.startTime,
            endTime = record.endTime,
            roomId = record.resource?.room?.id ?: 0L
        )
    }

    fun toResponseList(records: List<Records>): List<RecordResponse> {
        return records.map { toResponse(it) }
    }
}
