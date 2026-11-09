package ru.bgpu.campus.booking.components

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.booking.dto.CreateRecordRequest
import ru.bgpu.campus.booking.exceptions.*
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.booking.repositories.RecordsRepository
import ru.bgpu.campus.core.models.AccessPoint
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Component
class RecordValidator(
    private val recordRepository: RecordsRepository,
    private val clock: Clock,

    @Value("\${booking.max-days-ahead:7}") private val maxDaysAhead: Long,
    @Value("\${booking.max-records-per-day:6}") private val maxBookingHoursPerDay: Long
) {

    // Единый публичный метод-фасад, который запускает все проверки по очереди
    fun validateAll(request: CreateRecordRequest, user: User, resource: Resource) {
        validateTimeInterval(request.startTime!!, request.endTime!!)
        validateBookingDates(request.bookingDate!!)
        validateUserRoom(roomNumber = user.room?.number)
        validateResourceStatus(resource, start = request.startTime!!, end = request.endTime!!)
        checkTimeOverlaps(resourceId = resource.id!!, request.bookingDate!!, start = request.startTime!!, end = request.endTime!!)
        checkUserDailyLimits(userId = user.id!!, request.bookingDate!!, start = request.startTime!!, end = request.endTime!!)
        checkAccessPointPermissions(user, resource.accessPoint)
    }

    private fun validateTimeInterval(start: LocalTime, end: LocalTime) {
        if (!start.isBefore(end)) {
            throw BookingException(HttpStatus.BAD_REQUEST, "Время начала бронирования должно быть меньше времени окончания")
        }
    }

    private fun validateUserRoom(roomNumber: String?) {
        if (roomNumber == null) throw InvalidRoomException("INVALID_ROOM_RANGE")
    }

    private fun validateBookingDates(bookingDate: LocalDate) {
        val today = LocalDate.now(clock)
        if (bookingDate.isBefore(today)) {
            throw PastDateException("Нельзя бронировать прошедшую дату: $bookingDate")
        }
        if (bookingDate.isAfter(today.plusDays(maxDaysAhead))) {
            throw PastDateException("Бронирование доступно не более чем на $maxDaysAhead дней вперёд")
        }
    }

    private fun validateResourceStatus(resource: Resource, start: LocalTime, end: LocalTime) {
        if (resource.isDeleted) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс удален и недоступен")
        if (!resource.isAvailable) throw SlotNotAvailableException("Ресурс сейчас недоступен для бронирования (отключен)")
        if (start.isBefore(resource.activeFrom) || end.isAfter(resource.activeTo)) {
            throw SlotNotAvailableException("Выбранное время выходит за рамки работы ресурса")
        }
    }

    private fun checkTimeOverlaps(resourceId: Long, date: LocalDate, start: LocalTime, end: LocalTime) {
        val hasOverlap = recordRepository.existsOverlap(resourceId, date, start, end)
        if (hasOverlap) throw SlotNotAvailableException("Выбранное время уже занято другим пользователем")
    }

    private fun checkUserDailyLimits(userId: Long, date: LocalDate, start: LocalTime, end: LocalTime) {
        val dailyRecords = recordRepository.findAllByUserIdAndBookingDateAndStatus(userId, date, BookingStatus.CONFIRMED)
        val alreadyBookedMinutes = dailyRecords.fold(0L) { acc, record ->
            acc + Duration.between(record.startTime, record.endTime).toMinutes()
        }
        val newBookingMinutes = Duration.between(start, end).toMinutes()
        val maxBookingMinutesPerDay = (maxBookingHoursPerDay * 60).toLong()

        if (alreadyBookedMinutes + newBookingMinutes > maxBookingMinutesPerDay) {
            throw BookingLimitExceededException(
                "Превышен дневной лимит. Максимум: $maxBookingHoursPerDay ч. У вас занято: ${alreadyBookedMinutes / 60} ч. ${alreadyBookedMinutes % 60} мин."
            )
        }
    }

    private fun checkAccessPointPermissions(user: User, accessPoint: AccessPoint?) {
        if (accessPoint != null) {
            val hasAccessToPoint = user.allowedAccessPoints.any { it.id == accessPoint.id }
            if (!hasAccessToPoint) {
                throw AccessDeniedException("У вас нет доступа к точке '${accessPoint.name}'")
            }
        }
    }
    fun validateCancelByUser(record: ru.bgpu.campus.booking.models.Records, requestingUser: ru.bgpu.campus.core.models.User, now: java.time.LocalDateTime) {
        // 1. Проверяем владельца брони (безопасно через ID)
        if (record.user?.id != requestingUser.id) {
            throw AccessDeniedException("Вы не можете отменить чужую запись")
        }

        // 2. Отменить можно только активную бронь. Статусы CANCELED, REJECTED, COMPLETED блокируются
        if (record.status != BookingStatus.CONFIRMED) {
            throw BookingException(
                HttpStatus.BAD_REQUEST,
                "Можно отменить только активную бронь. Текущий статус: ${record.status}"
            )
        }

        // 3. Нельзя отменять бронирования на прошедшие даты или если время брони уже началось/прошло
        val bookingStartDateTime = java.time.LocalDateTime.of(record.bookingDate, record.startTime)
        if (!bookingStartDateTime.isAfter(now)) {
            throw BookingException(
                HttpStatus.BAD_REQUEST,
                "Нельзя отменить бронь, которая уже началась или осталась в прошлом"
            )
        }
    }

    // Валидация отмены для администратора (для защиты истории)
    fun validateRejectByAdmin(record: ru.bgpu.campus.booking.models.Records) {
        // Админ не должен портить историю завершенных, отмененных или уже отклоненных броней
        if (record.status != ru.bgpu.campus.booking.models.BookingStatus.CONFIRMED) {
            throw BookingException(
               HttpStatus.BAD_REQUEST,
                "Администратор может отклонить только активную бронь. Текущий статус: ${record.status}"
            )
        }
    }
}
