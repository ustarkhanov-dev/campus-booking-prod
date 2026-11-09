package ru.bgpu.campus.booking.services

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.repositories.AccessPointRepository
import ru.bgpu.campus.booking.dto.CreateResourceRequest
import ru.bgpu.campus.booking.exceptions.BookingException
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.booking.repositories.ResourceRepository
import ru.bgpu.campus.core.repositories.RoomRepository

@Service
class ResourceService(

    private val resourceRepository: ResourceRepository,
    private val accessPointRepository: AccessPointRepository,
    private val roomRepository: RoomRepository
) {

    fun findAll(): List<Resource> {

        return resourceRepository.findAllByIsDeletedFalse()
    }

    fun findByAccessPointId(accessPointId: Long): List<Resource> {
        return resourceRepository.findAllByAccessPointIdAndIsDeletedFalse(accessPointId)
    }

    fun findAllForUser(user: User): List<Resource> {
        val pointIds = user.allowedAccessPoints.mapNotNull { it.id }
        if (pointIds.isEmpty()) {
            return emptyList()
        } else {
            return resourceRepository.findAllByAccessPointIdInAndIsDeletedFalse(pointIds)
        }
    }



    @Transactional
    fun toggleResourceAvailability(id: Long): Resource {
        val resource = resourceRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден: $id") }

        if (resource.isDeleted) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс был удален")
        }

        resource.isAvailable = !resource.isAvailable
        return resourceRepository.save(resource)
    }

    @Transactional
    fun createResource(request: CreateResourceRequest): Resource {

        val accessPoint = accessPointRepository.findById(request.accessPointId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Точка доступа не найдена: ${request.accessPointId}") }

        val roomId = request.roomId
            ?: throw BookingException(HttpStatus.BAD_REQUEST, "ID комнаты обязателен")


        val room = roomRepository.findByIdOrNull(roomId)
            ?: throw BookingException(HttpStatus.NOT_FOUND, "Комната с ID $roomId не найдена")


        val resource = Resource(
            name = request.name,
            isAvailable = true,
            activeFrom = request.activeFrom,
            activeTo = request.activeTo,
            accessPoint = accessPoint,


            room = room,
            accessType = request.accessType,
            allowedFacultyIds = request.allowedFacultyIds,
            description = request.description,
            rules = request.rules,
            isDeleted = false
        )


        return resourceRepository.save(resource)
    }


    @Transactional
    fun deleteResource(id: Long) {
        val resource = resourceRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден: $id") }


        resource.isDeleted = true
        resourceRepository.save(resource)
    }
}
