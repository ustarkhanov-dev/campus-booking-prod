package ru.bgpu.campus.booking.dto

import jakarta.validation.constraints.NotNull

data class UpdateRoomRequest(
    @field:NotNull(message = "ID комнаты (roomId) обязателен")
    val roomId: Long
)