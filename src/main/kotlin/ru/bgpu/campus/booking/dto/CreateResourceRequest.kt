package ru.bgpu.campus.booking.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.bgpu.campus.booking.models.ResourceAccessType
import java.time.LocalTime

data class CreateResourceRequest(
    @field:NotBlank(message = "Название ресурса обязательно")
    val name: String,

    @field:NotNull(message = "ID точки доступа (Access Point) обязателен")
    val accessPointId: Long,

    @field:NotNull(message = "ID комнаты (Room ID) обязателен")
    val roomId: Long?,

    // Новые поля политики доступа и ограничений
    val accessType: ResourceAccessType = ResourceAccessType.CAMPUS_DORM_COMMON,
    val allowedFacultyIds: Set<Long> = emptySet(),

    // Текстовые поля
    val description: String? = null,
    val rules: String? = null,

    // Время работы (по умолчанию с 00:00 до 23:59)
    val activeFrom: LocalTime = LocalTime.of(0, 0),
    val activeTo: LocalTime = LocalTime.of(23, 59)
)
