package ru.bgpu.campus.booking.services

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import ru.bgpu.campus.booking.dto.SlotStatus
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Records
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.booking.repositories.RecordsRepository
import ru.bgpu.campus.booking.repositories.ResourceRepository
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.User
import java.time.LocalDate
import java.time.LocalTime

class DynamicBookingServiceTest {

    private val resourceRepository: ResourceRepository = mockk()
    private val recordsRepository: RecordsRepository = mockk()
    private val dynamicBookingService = DynamicBookingService(resourceRepository, recordsRepository)
    @BeforeEach
    fun setUp() {
        clearMocks(resourceRepository, recordsRepository)
    }
    @Test
    fun `should generate 24 hours with correct statuses (Disabled, Booked, Available)`() {
        val testDate = LocalDate.of(2026, 10, 25)
        // Стиралка работает с 07:00 до 23:00
        val washingMachine = Resource(
            id = 1L,
            name = "Стиралка №1",
            isAvailable = true,
            activeFrom = LocalTime.of(7, 0),
            activeTo = LocalTime.of(23, 0),
            room = mockk()
        )

        val student = User(id = 5L, name = "Иван", surname = "Иванов", room = Room(number = "302"))
        // Бронь на 14:00 - 15:00
        val existingBooking = Records(
            id = 100L,
            bookingDate = testDate,
            startTime = LocalTime.of(14, 0),
            endTime = LocalTime.of(15, 0),
            status = BookingStatus.CONFIRMED,
            user = student,
            resource = washingMachine
        )

        every { resourceRepository.findByIdOrNull(1L) } returns washingMachine
        every { recordsRepository.findAllByResourceIdAndBookingDateAndStatus(1L, testDate, BookingStatus.CONFIRMED) } returns listOf(existingBooking)

        // Act: Получаем суточную сетку
        val schedule = dynamicBookingService.getDailySchedule(1L, testDate)

        // Assert: Ровно 24 слота
        assertEquals(24, schedule.size)

        // Ночь (03:00 - 04:00) должна быть DISABLED
        assertEquals(SlotStatus.DISABLED, schedule[3].status)

        // Рабочее свободное утро (09:00 - 10:00) должно быть AVAILABLE
        assertEquals(SlotStatus.AVAILABLE, schedule[9].status)

        // Занятый час (14:00 - 15:00) должен быть BOOKED с данными студента
        val bookedSlot = schedule[14]
        assertEquals(SlotStatus.BOOKED, bookedSlot.status)
        assertEquals("Иванов Иван (Комната 302)", bookedSlot.bookingDetails?.studentName)
    }

}