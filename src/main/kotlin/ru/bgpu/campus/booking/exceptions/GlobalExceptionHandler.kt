package ru.bgpu.campus.booking.exceptions

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String
)

@RestControllerAdvice
class GlobalExceptionHandler(

    private val clock: Clock
) {


    @ExceptionHandler(BookingException::class)
    fun handleBookingException(ex: BookingException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(clock),
            status = ex.statusCode.value(),
            error = ex.statusCode.toString(),
            message = ex.reason ?: "Ошибка бронирования"
        )
        return ResponseEntity(errorResponse, ex.statusCode)
    }
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(clock),
            status = ex.statusCode.value(),
            error = ex.statusCode.toString(),
            message = ex.reason ?: ex.message
        )
        return ResponseEntity(errorResponse, ex.statusCode)
    }


    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(clock),
            status = 400,
            error = "Bad Request",
            message = ex.message ?: "Unknown error"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException::class)
    fun handleValidationException(ex: org.springframework.web.bind.MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(clock),
            status = 400,
            error = "Bad Request",
            message = "Ошибка валидации входных данных"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(clock),
            status = 500,
            error = "Internal Server Error",
            message = "Произошла внутренняя ошибка сервера. Обратитесь к администратору."
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<Map<String, String>> {
        // Ищем в тексте ошибки имя нашего ограничения "no_overlap"
        if (ex.mostSpecificCause.message?.contains("no_overlap") == true) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT) // Отдаем статус 409 Conflict
                .body(mapOf("error" to "Выбранное время уже забронировано другим пользователем (Контроль СУБД)"))
        }

        // Для всех остальных непредвиденных ошибок целостности данных (например, дубликаты ключей)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to "Ошибка нарушения целостности данных: ${ex.mostSpecificCause.message}"))
    }
}

