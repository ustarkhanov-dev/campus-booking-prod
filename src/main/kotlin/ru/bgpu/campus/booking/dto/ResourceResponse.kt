package ru.bgpu.campus.booking.dto

import ru.bgpu.campus.booking.models.ResourceAccessType
import java.time.LocalTime

data class ResourceResponse(
    val id: Long,
    val name: String,
    val isAvailable: Boolean,
    val activeFrom: LocalTime,
    val activeTo: LocalTime,
    val accessPointId: Long?,
    val accessPointName: String?,

    // Наши новые архитектурные поля
    val roomId: Long,
    val accessType: ResourceAccessType,
    val allowedFacultyIds: Set<Long>,
    val description: String?,
    val rules: String?
)
