package ru.bgpu.campus.core.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.core.dto.CreateRoomRequest
import ru.bgpu.campus.core.dto.GenerateRoomRangeRequest
import ru.bgpu.campus.core.dto.RoomResponse
import ru.bgpu.campus.core.models.RoomType
import ru.bgpu.campus.core.services.RoomService

@RestController
@RequestMapping("/admin/rooms")
@Tag(name = "Админ Комнаты", description = "Управление жилыми и служебными помещениями кампуса")
class AdminRoomController(private val roomService: RoomService) {

    @PostMapping
    @Operation(summary = "Штучное создание помещения (например, Прачечной)")
    fun createRoom(@Valid @RequestBody request: CreateRoomRequest): ResponseEntity<RoomResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request))
    }

    @PostMapping("/generate-range")
    @Operation(summary = "Массовая генерация диапазона комнат (например, 21..40 на 2 этаже)")
    fun generateRange(@Valid @RequestBody request: GenerateRoomRangeRequest): ResponseEntity<List<RoomResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.generateRange(request))
    }

    @GetMapping
    @Operation(summary = "Получение списка комнат корпуса с фильтром по типу")
    fun getRooms(
        @RequestParam accessPointId: Long,
        @RequestParam(required = false) type: RoomType?
    ): ResponseEntity<List<RoomResponse>> {
        return ResponseEntity.ok(roomService.findByAccessPoint(accessPointId, type))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Безопасное удаление комнаты по ID")
    fun deleteRoom(@PathVariable id: Long): ResponseEntity<Void> {
        roomService.deleteRoom(id)
        return ResponseEntity.noContent().build()
    }
}