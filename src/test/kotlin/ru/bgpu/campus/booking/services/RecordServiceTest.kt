package ru.bgpu.campus.booking.services

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import ru.bgpu.campus.booking.components.RecordValidator
import ru.bgpu.campus.booking.dto.CreateRecordRequest
import ru.bgpu.campus.booking.exceptions.AccessDeniedException
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Records
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.booking.repositories.RecordsRepository
import ru.bgpu.campus.booking.repositories.ResourceRepository
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.repositories.UserRepository
import java.time.*

class RecordServiceTest {

    private val recordRepository: RecordsRepository = mockk(relaxed = true)
    private val resourceRepository: ResourceRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val recordValidator: RecordValidator = mockk(relaxed = true)
    private val fixedClock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneId.of("UTC"))

    private val recordService = RecordService(
        recordRepository, resourceRepository, userRepository, fixedClock, recordValidator
    )

    @Test
    fun `createRecord should validate and save record`() {
        val user = User(id = 1L, username = "ivanov")
        val resource = Resource(id = 1L, name = "Стиралка", room = mockk())
        val request = CreateRecordRequest(1L, LocalDate.of(2026, 10, 21), LocalTime.of(12, 0), LocalTime.of(14, 0))

        every { userRepository.findUserByUsername("ivanov") } returns user
        every { resourceRepository.findByIdOrNull(1L) } returns resource
        every { recordRepository.save(any()) } answers { firstArg() }

        val created = recordService.createRecord(request, "ivanov")

        assertNotNull(created)
        verify(exactly = 1) { recordValidator.validateAll(request, user, resource) }
        verify(exactly = 1) { recordRepository.save(any()) }
    }

    @Test
    fun `cancelMyRecord should fail when trying to cancel foreign booking`() {
        val owner = User(id = 1L, username = "owner")
        val hacker = User(id = 2L, username = "hacker")
        val record = Records(id = 55L, user = owner, status = BookingStatus.CONFIRMED, bookingDate = LocalDate.now(), startTime = LocalTime.NOON, endTime = LocalTime.MIDNIGHT)

        every { recordRepository.findByIdOrNull(55L) } returns record
        every { userRepository.findUserByUsername("hacker") } returns hacker
        // Валидатор выбросит ошибку доступа, если ID юзера не совпадает с владельцем
        every { recordValidator.validateCancelByUser(record, hacker, any()) } throws AccessDeniedException("Вы не можете отменить чужую запись")

        assertThrows<AccessDeniedException> {
            recordService.cancelMyRecord(55L, "hacker")
        }
        // Убеждаемся, что статус НЕ поменялся на CANCELED
        assertEquals(BookingStatus.CONFIRMED, record.status)
    }

    @BeforeEach
    fun setUp() {
        // Очищаем историю вызовов моков перед каждым тестом
        clearMocks(recordRepository, resourceRepository, userRepository, recordValidator)

        // Защита от ClassCastException при вызове recordRepository.save(...)
        every { recordRepository.save(any()) } answers { firstArg() }
    }

}