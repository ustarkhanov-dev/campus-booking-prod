package ru.bgpu.campus.booking.dto

import ru.bgpu.campus.booking.models.BookingStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class RecordResponse(
    val id: Long?,
    val bookingDate: LocalDate,
    val recordDate: LocalDateTime,
    val username: String,
    val resourceName: String,
    val status: BookingStatus,

    // Новые поля динамической системы
    val startTime: LocalTime,
    val endTime: LocalTime,
    val roomId: Long?
)