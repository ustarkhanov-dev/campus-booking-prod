package ru.bgpu.campus.booking.dto

import java.time.LocalTime
import ru.bgpu.campus.booking.models.BookingStatus

enum class SlotStatus {
    AVAILABLE, // Свободно для бронирования
    BOOKED,    // Занято студентом
    DISABLED   // Закрыто по умолчанию (ночное время / ресурс отключен)
}

data class DynamicSlotDto(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: SlotStatus,
    val bookingDetails: BookingInfo? = null
)

data class BookingInfo(
    val recordId: Long,
    val studentId: Long,
    val studentName: String,
    val status: BookingStatus
)
