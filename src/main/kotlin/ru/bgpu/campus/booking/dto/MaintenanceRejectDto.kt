package ru.bgpu.campus.booking.dto


import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class MaintenanceRejectDto(
    @field:NotNull(message = "ID ресурса обязателен")
    val resourceId: Long,

    @field:NotNull(message = "Дата обязательна")
    val date: LocalDate,

    @field:NotNull(message = "Время начала обязательно")
    val startTime: LocalTime,

    @field:NotNull(message = "Время окончания обязательно")
    val endTime: LocalTime
)
