package ru.bgpu.campus.core.dto

import ru.bgpu.campus.core.models.User
import java.time.LocalDateTime

data class AdminUserDto(
    val id: Long?,
    val role: String,
    val username: String,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val description: String?,
    val groups: String,
    val lastSyncedAt: LocalDateTime?,
    val roomNumber: String?,
    val allowedAccessPointIds: List<Long>
)


fun User.toAdminDto() = AdminUserDto(
    id = this.id,
    role = this.role,
    username = this.username,
    surname = this.surname,
    name = this.name,
    patronymic = this.patronymic,
    description = this.description,
    groups = this.groups,
    lastSyncedAt = this.lastSyncedAt,
    roomNumber = this.room?.number,
    allowedAccessPointIds = this.allowedAccessPoints.map { it.id ?: 0L }
)

