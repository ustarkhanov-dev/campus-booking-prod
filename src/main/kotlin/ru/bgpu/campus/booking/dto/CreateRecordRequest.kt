package ru.bgpu.campus.booking.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

data class CreateRecordRequest(
    @field:NotNull(message = "ID ресурса обязателен")
    val resourceId: Long?, // Делаем nullable, чтобы валидатор поймал null и вывел ваш message

    @field:NotNull(message = "Дата бронирования обязательна")
    val bookingDate: LocalDate?,

    @field:NotNull(message = "Время начала обязательно")
    val startTime: LocalTime?,

    @field:NotNull(message = "Время окончания обязательно")
    val endTime: LocalTime?)

