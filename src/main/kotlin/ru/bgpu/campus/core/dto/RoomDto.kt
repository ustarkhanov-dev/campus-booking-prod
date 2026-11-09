package ru.bgpu.campus.core.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.RoomType


data class CreateRoomRequest(
    val number: String? = null,
    val name: String? = null,
    @field:NotNull(message = "Этаж обязателен")
    val floor: Int,
    val type: RoomType = RoomType.RESIDENTIAL,
    @field:NotNull(message = "ID корпуса обязателен")
    val accessPointId: Long
)


data class GenerateRoomRangeRequest(
    @field:NotNull(message = "ID корпуса обязателен")
    val accessPointId: Long,
    @field:NotNull(message = "Этаж обязателен")
    val floor: Int,
    @field:Positive(message = "Начальный номер должен быть > 0")
    val fromNumber: Int,
    @field:Positive(message = "Конечный номер должен быть > 0")
    val toNumber: Int,
    val type: RoomType = RoomType.RESIDENTIAL
)


data class RoomResponse(
    val id: Long,
    val number: String?,
    val name: String?,
    val floor: Int,
    val type: RoomType,
    val accessPointId: Long,
    val accessPointName: String
)


fun Room.toResponse() = RoomResponse(
    id = this.id ?: 0L,
    number = this.number,
    name = this.name,
    floor = this.floor,
    type = this.type,
    accessPointId = this.accessPoint?.id ?: 0L,
    accessPointName = this.accessPoint?.name ?: "Неизвестный корпус"
)