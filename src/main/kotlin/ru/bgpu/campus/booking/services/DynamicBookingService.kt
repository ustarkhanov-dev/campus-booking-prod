package ru.bgpu.campus.booking.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.bgpu.campus.booking.dto.BookingInfo
import ru.bgpu.campus.booking.dto.DynamicSlotDto
import ru.bgpu.campus.booking.dto.SlotStatus
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.repositories.RecordsRepository
import ru.bgpu.campus.booking.repositories.ResourceRepository
import java.time.LocalDate
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalTime

@Service
class DynamicBookingService(
    private val resourceRepository: ResourceRepository,
    private val recordsRepository: RecordsRepository
) {


    @Transactional(readOnly = true)
    fun getDailySchedule(resourceId: Long, date: LocalDate): List<DynamicSlotDto> {

        val resource = resourceRepository.findByIdOrNull(resourceId)
            ?.takeIf { !it.isDeleted }
            ?: throw RuntimeException("Ресурс не найден или удален с ID: $resourceId")


        val activeRecords = recordsRepository.findAllByResourceIdAndBookingDateAndStatus(
            resourceId = resourceId,
            bookingDate = date,
            status = BookingStatus.CONFIRMED
        )


        return List(24) { hour ->
            val currentStart = LocalTime.of(hour, 0)
            val currentEnd = currentStart.plusHours(1)


            val isWorkingHour = resource.isAvailable &&
                    !currentStart.isBefore(resource.activeFrom) &&
                    !currentEnd.isAfter(resource.activeTo)

            if (!isWorkingHour) {

                DynamicSlotDto(
                    startTime = currentStart,
                    endTime = currentEnd,
                    status = SlotStatus.DISABLED,
                    bookingDetails = null
                )
            } else {

                val matchingRecord = activeRecords.find { record ->
                    record.startTime.isBefore(currentEnd) && record.endTime.isAfter(currentStart)
                }

                if (matchingRecord != null) {
                    val user = matchingRecord.user

                    val displayInfo = if (user != null) {
                        val fullName = "${user.surname} ${user.name}".trim()
                        val roomInfo = user.room?.number?.let { " (Комната $it)" } ?: " (Комната не указана)"
                        fullName + roomInfo
                    } else {
                        "Неизвестный студент"
                    }

                    DynamicSlotDto(
                        startTime = currentStart,
                        endTime = currentEnd,
                        status = SlotStatus.BOOKED,
                        bookingDetails = BookingInfo(
                            recordId = matchingRecord.id ?: 0L,
                            studentId = user?.id ?: 0L,
                            studentName = displayInfo,
                            status = matchingRecord.status
                        )
                    )
                } else {

                    DynamicSlotDto(
                        startTime = currentStart,
                        endTime = currentEnd,
                        status = SlotStatus.AVAILABLE,
                        bookingDetails = null
                    )
                }
            }
        }
    }


}
