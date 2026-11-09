package ru.bgpu.campus.booking.controllers.user_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.booking.dto.CreateRecordRequest
import ru.bgpu.campus.booking.dto.RecordResponse
import ru.bgpu.campus.booking.mapper.RecordMapper
import ru.bgpu.campus.booking.services.RecordService
import java.security.Principal

@RestController
@RequestMapping("/record")

@Tag(name = "Студенты Бронирования", description = "Управление записями и бронированием для студентов")
class RecordController(private val recordServer: RecordService) {

    @PostMapping
    @Operation(summary = "Создание нового бронирования")
    fun createRecord(
        @Valid @RequestBody request: CreateRecordRequest,
        principal: Principal // ИСПРАВЛЕНО: Spring Security сам закинет сюда данные авторизации
    ): ResponseEntity<RecordResponse> {
        // Из principal.name мы гарантированно получаем уникальный username/логин студента
        val entity = recordServer.createRecord(request, principal.name)
        return ResponseEntity.ok(RecordMapper.toResponse(entity))
    }

    @GetMapping("/my")
    @Operation(summary = "Получение истории бронирований текущего студента")
    fun getMyRecords(
        principal: Principal // ИСПРАВЛЕНО: Никаких ручных заголовков в аргументах
    ): ResponseEntity<List<RecordResponse>> {
        val entities = recordServer.findByUsername(principal.name)
        return ResponseEntity.ok(RecordMapper.toResponseList(entities))
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Отмена бронирования студентом")
    fun cancelRecord(
        @PathVariable id: Long,
        principal: Principal // ИСПРАВЛЕНО: Безопасно получаем имя пользователя из контекста
    ): ResponseEntity<Void> {
        recordServer.cancelMyRecord(id = id, currentUsername = principal.name)
        return ResponseEntity.noContent().build()
    }
}
