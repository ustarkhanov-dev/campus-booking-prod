package ru.bgpu.campus.booking.controllers.user_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.booking.dto.DynamicSlotDto
import ru.bgpu.campus.booking.dto.ResourceResponse
import ru.bgpu.campus.booking.mapper.ResourceMapper
import ru.bgpu.campus.booking.services.DynamicBookingService
import ru.bgpu.campus.booking.services.ResourceService
import ru.bgpu.campus.core.services.UserService
import java.security.Principal

@RestController
@RequestMapping("/resources")
@Tag(name = "Студенты: Ресурсы", description = "Просмотр доступных ресурсов для студентов")
class UserResourceController(
    private val resourceService: ResourceService,
    private val userService: UserService,
    private val dynamicBookingService: DynamicBookingService
) {

    @GetMapping
    @Operation(
        summary = "Получение списка доступных ресурсов студента",
        description = "Возвращает только те ресурсы, к зданиям/точкам доступа которых у текущего студента есть явное разрешение."
    )
    fun getAvailableResources(
        @RequestParam(required = false) accessPointId: Long?,
        principal: Principal // Безопасно получаем авторизованного студента
    ): ResponseEntity<List<ResourceResponse>> {

        val user = userService.findUserByUsername(principal.name)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")


        val resources = if (accessPointId != null) {
            // Если студент запрашивает конкретную точку, проверяем, разрешена ли она ему
            val hasAccess = user.allowedAccessPoints.any { it.id == accessPointId }
            if (!hasAccess) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "У вас нет доступа к точке с id=$accessPointId")
            }
            resourceService.findByAccessPointId(accessPointId)
        } else {
            // Если фильтра нет, отдаем только разрешенные ресурсы по умолчанию
            resourceService.findAllForUser(user)
        }

        return ResponseEntity.ok(ResourceMapper.toResponseList(resources))
    }
    @GetMapping("/{id}/schedule")
    @Operation(
        summary = "Получение динамической сетки расписания ресурса на день",
        description = "Принимает ID ресурса и дату. Возвращает нарезанные 24 часа с актуальным статусом занятости."
    )
    fun getResourceSchedule(
        @PathVariable id: Long,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) date: java.time.LocalDate
    ): ResponseEntity<List<DynamicSlotDto>> {

        // Вызываем сервис, который нарезает часы в памяти и сверяет с базой данных records
        val schedule = dynamicBookingService.getDailySchedule(resourceId = id, date = date)

        return ResponseEntity.ok(schedule)
    }
}
