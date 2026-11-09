package ru.bgpu.campus.core.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import ru.bgpu.campus.core.dto.ExternalUserInfoDto
import ru.bgpu.campus.core.dto.UserAccessDto
import ru.bgpu.campus.core.models.AccessPoint
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.repositories.AccessPointRepository
import ru.bgpu.campus.core.repositories.UserRepository
import ru.bgpu.campus.core.security.JwtProvider
import java.time.Clock
import java.time.LocalDateTime

@Service
class UserIntegrationService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate,
    private val accessPointRepository: AccessPointRepository,
    private val clock: Clock,

    @Value("\${bgpu.user-api.url}")
    private val externalApiUrl: String,
    @Value("\${bgpu.points-api.url}")
    private val pointsApiUrl: String
) {
    private val log = LoggerFactory.getLogger(UserIntegrationService::class.java)


    @Transactional
    fun getOrSyncUser(rawToken: String): User {

        val token = if (rawToken.startsWith("Bearer ")) rawToken.substring(7) else rawToken

        val claims = jwtProvider.getAccessClaims(token)
        val username = claims.get("login", String::class.java)
            ?: claims.subject
            ?: throw BadCredentialsException("Токен не содержит имени пользователя")

        val rolesList = claims.get("roles", List::class.java) as? List<*> ?: listOf("USER")
        val isAdmin = rolesList.any { it.toString().uppercase().contains("ADMIN") }

        val entity = HttpEntity<Unit>(HttpHeaders().apply { setBearerAuth(token) })

        val localUser = userRepository.findUserByUsernameWithAccessPoints(username)

        return if (localUser == null) {

            registerNewUser(username, entity, isAdmin)
        } else {

            syncExistingUserIfNeeded(localUser, username, entity, isAdmin)
        }
    }

    private fun registerNewUser(
        username: String,
        entity: HttpEntity<Unit>,
        isAdmin: Boolean
    ): User {
        log.info("Регистрация нового пользователя через БГПУ: $username")


        val matchedPoints = if (isAdmin) emptyList() else fetchAndFilterAccessPoints(username, entity)

        if (matchedPoints.isEmpty() && !isAdmin) {
            log.warn("Gatekeeper: Доступ отклонен для $username. Нет активных корпусов.")
            throw AccessDeniedException("Доступ запрещен: у вас нет прав на вход в активные корпуса кампуса.")
        }

        val response = restTemplate.exchange(externalApiUrl, HttpMethod.GET, entity, ExternalUserInfoDto::class.java)
        val dto = response.body ?: throw RuntimeException("Не удалось получить профиль из БГПУ")

        val newUser = User(
            username = username,
            surname = dto.surname,
            name = dto.name,
            patronymic = dto.patronymic,
            groups = dto.groups.joinToString(", "),
            role = if (isAdmin) "ADMIN" else "USER"
        ).apply {
            allowedAccessPoints = matchedPoints.toMutableList()
            lastSyncedAt = LocalDateTime.now(clock)
        }

        return userRepository.save(newUser)
    }

    private fun syncExistingUserIfNeeded(
        existingUser: User,
        username: String,
        entity: HttpEntity<Unit>,
        isAdmin: Boolean
    ): User {
        val now = LocalDateTime.now(clock)
        val syncedAt = existingUser.lastSyncedAt


        if (syncedAt == null || syncedAt.plusDays(1).isBefore(now)) {
            log.info("TTL истек: Обновление доступов из БГПУ для пользователя $username")

            try {

                val matchedPoints = if (isAdmin) emptyList() else fetchAndFilterAccessPoints(username, entity)

                if (matchedPoints.isEmpty() && !isAdmin) {
                    log.warn("Gatekeeper [Синхронизация]: У пользователя $username отозваны доступы.")
                    throw AccessDeniedException("Доступ запрещен: у вас больше нет прав на вход в корпуса кампуса.")
                }

                if (!isAdmin) {
                    existingUser.allowedAccessPoints.clear()
                    existingUser.allowedAccessPoints.addAll(matchedPoints)
                }
                existingUser.lastSyncedAt = now

                return userRepository.save(existingUser)
            } catch (e: AccessDeniedException) {
                throw e
            } catch (e: Exception) {

                log.warn("БГПУ недоступно при обновлении доступов для $username. Используются кэшированные права. Причина: ${e.message}")
            }
        }
        return existingUser
    }

    private fun fetchAndFilterAccessPoints(username: String, entity: HttpEntity<Unit>): List<AccessPoint> {
        val ruleResponse = restTemplate.exchange(
            "$pointsApiUrl?username=$username",
            HttpMethod.GET,
            entity,
            Array<UserAccessDto>::class.java
        )

        val externalIds = ruleResponse.body?.filter { building ->
            !building.userAccess.isNullOrEmpty() || !building.categoryAccess.isNullOrEmpty()
        }?.mapNotNull { building -> building.id } ?: emptyList()

        return externalIds
            .mapNotNull { extId -> accessPointRepository.findByExternalId(extId) }
            .filter { !it.archival }
    }
}
