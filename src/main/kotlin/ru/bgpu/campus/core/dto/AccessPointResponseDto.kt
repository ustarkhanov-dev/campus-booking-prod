package ru.bgpu.campus.core.dto

import ru.bgpu.campus.core.models.AccessPoint

data class AccessPointResponseDto(
    val id: Long?,
    val externalId: Long?,
    val name: String,
    val shortName: String,
    val archival: Boolean,
    val tags: String?
)


fun AccessPoint.toResponseDto() = AccessPointResponseDto(
    id = this.id,
    externalId = this.externalId,
    name = this.name,
    shortName = this.shortName,
    archival = this.archival,
    tags = this.tags
)
