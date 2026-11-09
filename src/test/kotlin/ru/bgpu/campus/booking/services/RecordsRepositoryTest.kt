package ru.bgpu.campus.booking.repositories

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.TestPropertySource
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Records
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.booking.models.ResourceAccessType
import ru.bgpu.campus.core.models.AccessPoint
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.RoomType
import ru.bgpu.campus.core.models.User
import java.time.LocalDate
import java.time.LocalTime

@DataJpaTest
@TestPropertySource(properties = [
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
])
class RecordsRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val recordsRepository: RecordsRepository
) {

    private lateinit var testUser: User
    private lateinit var testResource: Resource
    private val targetDate: LocalDate = LocalDate.of(2026, 11, 1)

    @BeforeEach
    fun setupData() {
        val accessPoint = AccessPoint(name = "Общежитие №2", shortName = "2", externalId = 6L)
        entityManager.persist(accessPoint)

        val room = Room(number = "204", floor = 2, type = RoomType.RESIDENTIAL, accessPoint = accessPoint)
        entityManager.persist(room)

        testUser = User(username = "petrov", name = "Петр", surname = "Петров", room = room)
        entityManager.persist(testUser)

        testResource = Resource(
            name = "Стиральная машина №1",
            activeFrom = LocalTime.of(7, 0),
            activeTo = LocalTime.of(23, 0),
            room = room,
            accessPoint = accessPoint,
            accessType = ResourceAccessType.CAMPUS_DORM_COMMON
        )
        entityManager.persist(testResource)
        entityManager.flush()
    }

    @Test
    @DisplayName("SQL existsOverlap: должен находить пересечения интервалов времени и игнорировать стыковые часы")
    fun `existsOverlap should detect collision and ignore adjacent slots`() {
        val existingRecord = Records(
            bookingDate = targetDate,
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(14, 0),
            status = BookingStatus.CONFIRMED,
            user = testUser,
            resource = testResource
        )
        entityManager.persist(existingRecord)
        entityManager.flush()

        val resourceId = testResource.id!!

        val hasOverlap1 = recordsRepository.existsOverlap(
            resourceId, targetDate, LocalTime.of(13, 0), LocalTime.of(15, 0)
        )
        assertTrue(hasOverlap1)

        val hasOverlap2 = recordsRepository.existsOverlap(
            resourceId, targetDate, LocalTime.of(12, 30), LocalTime.of(13, 30)
        )
        assertTrue(hasOverlap2)

        val hasOverlapAfter = recordsRepository.existsOverlap(
            resourceId, targetDate, LocalTime.of(14, 0), LocalTime.of(16, 0)
        )
        assertFalse(hasOverlapAfter)

        val hasOverlapBefore = recordsRepository.existsOverlap(
            resourceId, targetDate, LocalTime.of(10, 0), LocalTime.of(12, 0)
        )
        assertFalse(hasOverlapBefore)

        val hasOverlapOtherDay = recordsRepository.existsOverlap(
            resourceId, targetDate.plusDays(1), LocalTime.of(12, 0), LocalTime.of(14, 0)
        )
        assertFalse(hasOverlapOtherDay)
    }

    @Test
    @DisplayName("SQL Modifying Update: автозавершение истекших броней и проверка состояния в БД")
    fun `autoCompleteExpiredBookings should complete past bookings and keep future active`() {
        val today = LocalDate.of(2026, 11, 2)
        val nowTime = LocalTime.of(15, 0)

        val pastRecord = Records(
            bookingDate = today.minusDays(1),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            status = BookingStatus.CONFIRMED,
            user = testUser,
            resource = testResource
        )

        val todayExpiredRecord = Records(
            bookingDate = today,
            startTime = LocalTime.of(13, 0),
            endTime = LocalTime.of(14, 0),
            status = BookingStatus.CONFIRMED,
            user = testUser,
            resource = testResource
        )

        val futureRecord = Records(
            bookingDate = today,
            startTime = LocalTime.of(18, 0),
            endTime = LocalTime.of(20, 0),
            status = BookingStatus.CONFIRMED,
            user = testUser,
            resource = testResource
        )

        entityManager.persist(pastRecord)
        entityManager.persist(todayExpiredRecord)
        entityManager.persist(futureRecord)
        entityManager.flush()

        val updatedCount = recordsRepository.autoCompleteExpiredBookings(today, nowTime)

        entityManager.clear()

        assertEquals(2, updatedCount)

        val reloadedPast = recordsRepository.findById(pastRecord.id!!).get()
        val reloadedTodayExpired = recordsRepository.findById(todayExpiredRecord.id!!).get()
        val reloadedFuture = recordsRepository.findById(futureRecord.id!!).get()

        assertEquals(BookingStatus.COMPLETED, reloadedPast.status)
        assertEquals(BookingStatus.COMPLETED, reloadedTodayExpired.status)
        assertEquals(BookingStatus.CONFIRMED, reloadedFuture.status)
    }
}