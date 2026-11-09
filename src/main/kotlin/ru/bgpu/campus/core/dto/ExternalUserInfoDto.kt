
package ru.bgpu.campus.core.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalUserInfoDto(
    val id: Int,
    val login: String,
    val name: String,
    val surname: String,
    val patronymic: String? = "",
    val description: String = "",
    val defaultPassword: Boolean = false,
    val qrAccess: Boolean = false,
    val photo: Any? = null,
    val validPhoto: Boolean = false,
    val qrAccessType: String = "default",
    val groups: List<String> = emptyList(),
    val management: List<Any> = emptyList()
)
