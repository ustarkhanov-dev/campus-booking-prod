package ru.bgpu.campus.booking.dto

data class AccessPointResponse(
    val id: Long,
    val externalId: Long,
    val name: String,
    val shortName: String,
    val archival: Boolean
)