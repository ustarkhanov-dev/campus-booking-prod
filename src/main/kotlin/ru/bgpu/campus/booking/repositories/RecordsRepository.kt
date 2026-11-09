package ru.bgpu.campus.booking.repositories

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Records
import java.time.LocalDate
import java.time.LocalTime

interface RecordsRepository : JpaRepository<Records, Long> {




    @EntityGraph(attributePaths = ["user"])
    fun findAllByResourceIdAndBookingDateAndStatus(
        resourceId: Long,
        bookingDate: LocalDate,
        status: BookingStatus
    ): List<Records>

    @Modifying
    @Query("""
        UPDATE Records r 
        SET r.status = ru.bgpu.campus.booking.models.BookingStatus.REJECTED 
        WHERE r.resource.id = :resourceId 
          AND r.bookingDate = :date 
          AND r.status = ru.bgpu.campus.booking.models.BookingStatus.CONFIRMED
          AND r.startTime < :endTime 
          AND r.endTime > :startTime
    """)
    fun rejectRecordsForMaintenance(
        @Param("resourceId") resourceId: Long,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    ): Int
    @Lock(LockModeType.PESSIMISTIC_WRITE)


    fun findAllByUserIdAndBookingDateAndStatus(
        userId: Long,
        bookingDate: LocalDate,
        status: BookingStatus
    ): List<Records>


    @Query("""
    select count(r) > 0 from Records r 
    where r.resource.id = :resourceId 
      and r.bookingDate = :date 
      and r.status = 'CONFIRMED'
      and r.startTime < :end 
      and r.endTime > :start
""")
    fun existsOverlap(resourceId: Long, date: LocalDate, start: LocalTime, end: LocalTime): Boolean

    fun findAllByUserUsernameAndStatus(username: String, status: BookingStatus): List<Records>


    @Modifying
    @Query("""
    UPDATE Records r 
    SET r.status = ru.bgpu.campus.booking.models.BookingStatus.COMPLETED 
    WHERE r.status = ru.bgpu.campus.booking.models.BookingStatus.CONFIRMED 
      AND (r.bookingDate < :today OR (r.bookingDate = :today AND r.endTime <= :nowTime))
""")
    fun autoCompleteExpiredBookings(
        @Param("today") today: LocalDate,
        @Param("nowTime") nowTime: LocalTime
    ): Int

    @Query("SELECT r FROM Records r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.resource",
        countQuery = "SELECT count(r) FROM Records r")
    fun findAllWithRelations(pageable: Pageable): Page<Records>

    @EntityGraph(attributePaths = ["user", "resource"])
    fun findAllByBookingDate(bookingDate: LocalDate, pageable: Pageable): Page<Records>


    @EntityGraph(attributePaths = ["user", "resource"])
    fun findByBookingDateBetween(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<Records>



}
