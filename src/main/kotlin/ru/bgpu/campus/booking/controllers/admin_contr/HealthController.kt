package ru.bgpu.campus.booking.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/admin/health")
// 1. Указываем понятное имя группы для технических эндпоинтов
@Tag(name = "Мониторинг", description = "Проверка технического состояния и доступности сервиса")
class HealthController {

    @GetMapping
    // 2. Описываем сам метод проверки работоспособности
    @Operation(
        summary = "Проверка статуса сервиса (Health Check)",
        description = "Возвращает текущий статус приложения (UP) и серверное время для проверки работоспособности."
    )
    fun checkHealth(): ResponseEntity<Map<String, Any>> {
        val statusInfo = mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now().toString()
        )
        return ResponseEntity.ok(statusInfo)
    }
}