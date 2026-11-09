package ru.bgpu.campus.core.controllers.user_contr

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.bgpu.campus.booking.dto.AccessPointResponse
import ru.bgpu.campus.booking.dto.UpdateRoomRequest
import ru.bgpu.campus.core.dto.UserProfileResponse
import ru.bgpu.campus.core.services.UserIntegrationService
import ru.bgpu.campus.core.services.UserService
import java.security.Principal

@RestController
@RequestMapping("/users")

@Tag(name = "Профиль пользователя", description = "Личный кабинет студента (управление своими данными)")
class UserController(private val userService: UserService, private val userIntegrationService: UserIntegrationService) {

    @GetMapping("/me")
    @Operation(summary = "Получение полной информации о профиле текущего пользователя")
    fun getCurrentUserFull(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<UserProfileResponse> {


        val user = userIntegrationService.getOrSyncUser(token)

        val response = UserProfileResponse.fromEntity(user)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me/access-points")
    @Operation(summary = "Получение доступных точек доступа текущего пользователя")
    fun getMyAccessPoints(principal: Principal): List<AccessPointResponse> {
        val user = userService.findUserByUsername(principal.name)
            ?: throw RuntimeException("Пользователь не найден")

        return user.allowedAccessPoints.map { ap ->
            AccessPointResponse(
                id = ap.id ?: 0,
                externalId = ap.externalId ?: 0,
                name = ap.name,
                shortName = ap.shortName,
                archival = ap.archival
            )
        }
    }

    @PutMapping("/room")
    @Operation(summary = "Обновление комнаты проживания студентом")
    fun updateMyRoom(
        principal: Principal,
        @Valid @RequestBody request: UpdateRoomRequest
    ): ResponseEntity<Map<String, String>> {
        val updatedRoomNumber = userService.updateUserRoom(principal.name, request.roomId)

        return ResponseEntity.ok(mapOf(
            "message" to "Комната успешно привязана к профилю",
            "room" to updatedRoomNumber
        ))
    }
}
