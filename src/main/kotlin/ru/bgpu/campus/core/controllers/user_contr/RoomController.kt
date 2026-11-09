package ru.bgpu.campus.core.controllers.user_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.core.dto.RoomResponse
import ru.bgpu.campus.core.models.RoomType
import ru.bgpu.campus.core.services.RoomService

@RestController
@RequestMapping("/rooms")
@Tag(name = "Студенты: Комнаты", description = "Выбор жилой комнаты для своего профиля")
class RoomController(private val roomService: RoomService) {

    @GetMapping
    @Operation(summary = "Получить список только ЖИЛЫХ комнат корпуса для выпадающего списка")
    fun getLivingRooms(@RequestParam accessPointId: Long): ResponseEntity<List<RoomResponse>> {

        return ResponseEntity.ok(roomService.findByAccessPoint(accessPointId, RoomType.RESIDENTIAL))
    }
}