package ru.bgpu.campus.core.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtProvider(@Value("\${bgpu.access-secret-key}") jwtAccessSecret: String) {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtAccessSecret.toByteArray())


    fun validateAccessToken(accessToken: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
            return true
        } catch (e: ExpiredJwtException) {
            log.error("Срок действия JWT токена истек: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            log.error("Неподдерживаемый формат JWT: {}", e.message)
        } catch (e: MalformedJwtException) {
            log.error("Искаженный/некорректный JWT токен: {}", e.message)
        } catch (e: SignatureException) {
            log.error("Неверная цифровая подпись JWT: {}", e.message)
        } catch (e: Exception) {
            log.error("Ошибка при валидации JWT токена: ", e)
        }
        return false
    }


    fun getAccessClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }


    fun getAuthentication(token: String): Authentication {
        val claims = getAccessClaims(token)
        val username = claims.get("login", String::class.java)
            ?: claims.subject
            ?: throw BadCredentialsException("Токен не содержит имени пользователя")

        val rolesList = claims.get("roles", List::class.java) as? List<*> ?: listOf("USER")
        val authorities = rolesList.map {
            val role = it.toString().uppercase()
            SimpleGrantedAuthority(if (role.startsWith("ROLE_")) role else "ROLE_$role")
        }


        val principal = User(username, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, null, authorities)
    }
}