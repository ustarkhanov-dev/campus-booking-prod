package ru.bgpu.campus.booking.mapper

import ru.bgpu.campus.booking.dto.ResourceResponse
import ru.bgpu.campus.booking.models.Resource

object ResourceMapper {


    fun toResponse(resource: Resource): ResourceResponse {
        return ResourceResponse(
            id = resource.id ?: 0L,
            name = resource.name,
            isAvailable = resource.isAvailable,
            activeFrom = resource.activeFrom,
            activeTo = resource.activeTo,
            accessPointId = resource.accessPoint?.id,
            accessPointName = resource.accessPoint?.name,


            roomId = resource.room.id ?: 0L,
            accessType = resource.accessType,
            allowedFacultyIds = resource.allowedFacultyIds,
            description = resource.description,
            rules = resource.rules
        )
    }

    fun toResponseList(resources: List<Resource>): List<ResourceResponse> {
        return resources.map { toResponse(it) }
    }



}
