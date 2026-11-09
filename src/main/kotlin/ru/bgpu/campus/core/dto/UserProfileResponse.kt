package ru.bgpu.campus.core.dto

import ru.bgpu.campus.core.models.User

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val surname: String,
    val name: String,
    val patronymic: String,
    val fullName: String,
    val description: String,
    val groups: List<String>,
    val role: String
) {
    companion object {
        fun fromEntity(user: User): UserProfileResponse {
            return UserProfileResponse(
                id = user.id ?: 0L,
                username = user.username,
                surname = user.surname,
                name = user.name,
                patronymic = user.patronymic ?: "",
                fullName = "${user.surname} ${user.name} ${user.patronymic ?: ""}".trim(),
                description = user.description ?: "",
                groups = user.groups.split(",").filter { it.isNotBlank() },
                role = user.role
            )
        }
    }
}
