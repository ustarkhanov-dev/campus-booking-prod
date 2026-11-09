package ru.bgpu.campus.core.services

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.booking.repositories.ResourceRepository
import ru.bgpu.campus.core.dto.CreateRoomRequest
import ru.bgpu.campus.core.dto.GenerateRoomRangeRequest
import ru.bgpu.campus.core.dto.RoomResponse
import ru.bgpu.campus.core.dto.toResponse
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.RoomType
import ru.bgpu.campus.core.repositories.AccessPointRepository
import ru.bgpu.campus.core.repositories.RoomRepository

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val accessPointRepository: AccessPointRepository,
    private val resourceRepository: ResourceRepository
) {


    @Transactional
    fun createRoom(request: CreateRoomRequest): RoomResponse {
        val accessPoint = accessPointRepository.findByIdOrNull(request.accessPointId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Точка доступа не найдена: ${request.accessPointId}")

        if (request.type == RoomType.RESIDENTIAL && request.number.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Для жилой комнаты номер обязателен")
        }
        if (request.type == RoomType.COMMON && request.name.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Для служебного помещения название обязательно")
        }

        val room = Room(
            number = request.number?.trim(),
            name = request.name?.trim(),
            floor = request.floor,
            type = request.type,
            accessPoint = accessPoint
        )

        return roomRepository.save(room).toResponse()
    }


    @Transactional
    fun generateRange(request: GenerateRoomRangeRequest): List<RoomResponse> {
        val accessPoint = accessPointRepository.findByIdOrNull(request.accessPointId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Точка доступа не найдена: ${request.accessPointId}")

        if (request.fromNumber > request.toNumber) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Начальный номер должен быть меньше конечного")
        }

        val roomsToCreate = mutableListOf<Room>()

        for (num in request.fromNumber..request.toNumber) {
            val numStr = num.toString()

            if (!roomRepository.existsByNumberAndAccessPointId(numStr, request.accessPointId)) {
                roomsToCreate.add(
                    Room(
                        number = numStr,
                        name = null,
                        floor = request.floor,
                        type = request.type,
                        accessPoint = accessPoint
                    )
                )
            }
        }

        val savedRooms = roomRepository.saveAll(roomsToCreate)
        return savedRooms.map { it.toResponse() }
    }


    @Transactional
    fun deleteRoom(id: Long) {
        val room = roomRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Комната с ID $id не найдена")

        if (resourceRepository.existsByRoomId(id)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Нельзя удалить комнату, так как в ней зарегистрировано оборудование (стиральные машины)!"
            )
        }

        roomRepository.delete(room)
    }

    @Transactional(readOnly = true)
    fun findByAccessPoint(accessPointId: Long, type: RoomType?): List<RoomResponse> {
        val rooms = if (type != null) {
            roomRepository.findAllByAccessPointIdAndTypeOrderByFloorAscNumberAsc(accessPointId, type)
        } else {
            roomRepository.findAllByAccessPointIdOrderByFloorAscNumberAsc(accessPointId)
        }
        return rooms.map { it.toResponse() }
    }
}