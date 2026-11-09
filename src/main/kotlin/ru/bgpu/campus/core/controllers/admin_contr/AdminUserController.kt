package ru.bgpu.campus.core.controllers.admin_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.core.dto.AdminUserDto
import ru.bgpu.campus.core.dto.toAdminDto
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.services.UserService

@RestController
@RequestMapping("/admin/users")

@Tag(name = "Администрирование: Пользователи", description = "Глобальное управление пользователями и лимитами системы")
class AdminUserController(private val userService: UserService) {

    @GetMapping
    @Operation(summary = "Получение списка всех пользователей (Для Админа)")
    fun findAll(): List<AdminUserDto> {
        return userService.findAll().map { it.toAdminDto() }
    }

    @GetMapping("/max-records")
    @Operation(summary = "Получение глобального лимита бронирований системы")
    fun getMaxRecords(): Int = userService.getMaxRecordsPerDay()


}
