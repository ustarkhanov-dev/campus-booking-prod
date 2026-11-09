package ru.bgpu.campus.booking.services

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.booking.components.RecordValidator
import ru.bgpu.campus.core.repositories.UserRepository
import ru.bgpu.campus.booking.dto.CreateRecordRequest
import ru.bgpu.campus.booking.exceptions.BookingException
import ru.bgpu.campus.booking.models.Records
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.repositories.RecordsRepository
import ru.bgpu.campus.booking.repositories.ResourceRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RecordService(
    private val recordRepository: RecordsRepository,
    private val resourceRepository: ResourceRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
    private val recordValidator: RecordValidator,

    ) {
    private val log = LoggerFactory.getLogger(this.javaClass)


    @Transactional
    fun createRecord(request: CreateRecordRequest, username: String): Records {
        log.info("Attempt to create record: resource=${request.resourceId}, date=${request.bookingDate}, time=${request.startTime}-${request.endTime}, user=$username")


        val user = userRepository.findUserByUsername(username)
            ?: throw BookingException(HttpStatus.NOT_FOUND, "Пользователь не найден")


        val resource = resourceRepository.findByIdOrNull(request.resourceId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ресурс не найден: ${request.resourceId}")

        val bookingDate = request.bookingDate ?: throw BookingException(HttpStatus.BAD_REQUEST, "Дата обязательна")
        val startTime = request.startTime ?: throw BookingException(HttpStatus.BAD_REQUEST, "Время начала обязательно")
        val endTime = request.endTime ?: throw BookingException(HttpStatus.BAD_REQUEST, "Время окончания обязательно")


        recordValidator.validateAll(request, user, resource)


        val record = Records(
            recordDate = LocalDateTime.now(clock),
            bookingDate = bookingDate,
            startTime = startTime,
            endTime = endTime,
            user = user,
            resource = resource
        )

        return recordRepository.save(record).also {
            log.info("Record successfully created with ID: ${it.id} for user: $username")
        }
    }

    fun findByUsername(username: String): List<Records> {
        return recordRepository.findAllByUserUsernameAndStatus(username, BookingStatus.CONFIRMED)
    }

        @Transactional
        fun cancelMyRecord(id: Long, currentUsername: String) {
            val record = recordRepository.findByIdOrNull(id)
                ?: throw BookingException(HttpStatus.NOT_FOUND, "Запись с ID $id не найдена")

            val user = userRepository.findUserByUsername(currentUsername)
                ?: throw BookingException(HttpStatus.NOT_FOUND, "Пользователь не найден")

            recordValidator.validateCancelByUser(record, user, LocalDateTime.now(clock))

            record.status = BookingStatus.CANCELED
            recordRepository.save(record)
            log.info("User $currentUsername successfully canceled record $id")
        }


    @Transactional
    fun cancelRecordAsAdmin(id: Long) {
        val record = recordRepository.findByIdOrNull(id)
            ?: throw BookingException(HttpStatus.NOT_FOUND, "Запись с ID $id не найдена")

        recordValidator.validateRejectByAdmin(record)

        record.status = BookingStatus.REJECTED
        recordRepository.save(record)
        log.info("Admin successfully rejected record $id")
    }

        @Transactional
        fun rejectForMaintenance(dto: ru.bgpu.campus.booking.dto.MaintenanceRejectDto) {

            if (dto.startTime.isAfter(dto.endTime) || dto.startTime == dto.endTime) {
                throw BookingException(
                    HttpStatus.BAD_REQUEST,
                    "Время начала должно быть строго меньше времени окончания"
                )
            }

            log.info("Запуск массовой отмены броней для ресурса ${dto.resourceId} на дату ${dto.date} в интервале ${dto.startTime}-${dto.endTime}")


            val updatedCount = recordRepository.rejectRecordsForMaintenance(
                resourceId = dto.resourceId,
                date = dto.date,
                startTime = dto.startTime,
                endTime = dto.endTime
            )

            log.info("Массовая отмена успешно завершена. Отменено записей: $updatedCount")
        }


    fun findAllWithRelations(pageable: Pageable): Page<Records> {
        return recordRepository.findAllWithRelations(pageable)
    }
    fun findAllByDate(date: LocalDate, pageable: Pageable): Page<Records> {
        return recordRepository.findAllByBookingDate(date, pageable)
    }

    fun findByBookingDateBetween(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<Records> {
        return recordRepository.findByBookingDateBetween(startDate, endDate, pageable)
    }





}


