package ru.bgpu.campus.booking.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.bgpu.campus.booking.dto.CreateResourceRequest
import ru.bgpu.campus.booking.dto.ResourceResponse
import ru.bgpu.campus.booking.mapper.ResourceMapper
import ru.bgpu.campus.booking.services.ResourceService
import ru.bgpu.campus.core.services.UserService

@RestController
@RequestMapping("/admin/resources")

// 1. Привязываем контроллер к общему красивому блоку ресурсов
@Tag(name = "Админ Ресурсы", description = "Управление инвентарем и ресурсами кампуса (стиральные машины, помещения и др.)")
class AdminResourceController(
    private val resourceService: ResourceService,
    private val userService: UserService
) {

    //переделывать
    @GetMapping
    @Operation(
        summary = "Получение списка всех ресурсов с фильтрацией",
        description = "Администратор видит абсолютно все ресурсы в системе. Можно отфильтровать по конкретной точке доступа."
    )
    fun getAllResources(
        @RequestParam(required = false) accessPointId: Long?
    ): ResponseEntity<List<ResourceResponse>> {

        val resources = if (accessPointId != null) {
            resourceService.findByAccessPointId(accessPointId)
        } else {
            resourceService.findAll()
        }

        return ResponseEntity.ok(ResourceMapper.toResponseList(resources))
    }

    @PatchMapping("/{id}/toggle")
    @Operation(
        summary = "Переключение доступности ресурса",
        description = "Изменяет статус активности ресурса (включает/выключает возможность бронирования по его ID)."
    )
    fun toggleAvailability(@PathVariable id: Long): ResponseEntity<ResourceResponse> {
        val resource = resourceService.toggleResourceAvailability(id)
        return ResponseEntity.ok(ResourceMapper.toResponse(resource))
    }

    @PostMapping
    @Operation(
        summary = "Создание нового ресурса",
        description = "Добавляет новый ресурс в систему. Данные передаются в теле запроса."
    )
    fun createResource(@RequestBody request: CreateResourceRequest): ResponseEntity<ResourceResponse> {
        val resource = resourceService.createResource(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ResourceMapper.toResponse(resource))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удаление ресурса",
        description = "Полностью удаляет ресурс из системы по его уникальному идентификатору."
    )
    fun deleteResource(@PathVariable id: Long): ResponseEntity<Void> {
        resourceService.deleteResource(id)
        return ResponseEntity.noContent().build()
    }
}