package ru.bgpu.campus.core.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.bgpu.campus.core.dto.AccessPointResponseDto
import ru.bgpu.campus.core.dto.toResponseDto
import ru.bgpu.campus.core.repositories.AccessPointRepository

@RestController
@RequestMapping("/admin/access-points")


@Tag(name = "Админ Точки доступа", description = "Управление пропускными пунктами кампуса (корпуса, общежития и т.д.)")
class AccessPointController(
    private val accessPointRepository: AccessPointRepository
) {

    @GetMapping
    @Operation(
        summary = "Получение всех точек доступа",
        description = "Возвращает полный список зарегистрированных точек доступа кампуса, включая архивные."
    )
    fun findAll(): List<AccessPointResponseDto> =
        accessPointRepository.findAll().map { it.toResponseDto() }

    @PutMapping("/{id}/toggle")
    @Operation(
        summary = "Инвертирование архивного статуса",
        description = "Переключает статус архивности точки доступа на противоположный по её ID."
    )
    fun toggle(@PathVariable id: Long): AccessPointResponseDto {
        val point = accessPointRepository.findById(id)
            .orElseThrow { RuntimeException("Точка доступа не найдена: $id") }

        point.archival = !point.archival
        val savedPoint = accessPointRepository.save(point)

        return savedPoint.toResponseDto()
    }


}