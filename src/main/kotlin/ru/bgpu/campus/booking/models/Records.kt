package ru.bgpu.campus.booking.models

import jakarta.persistence.*
import ru.bgpu.campus.core.models.User
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class BookingStatus {
    CONFIRMED,
    CANCELED,
    COMPLETED,
    REJECTED,
    NOSHOW
}

@Entity
@Table(name = "records")
class Records(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "record_date", nullable = false, updatable = false)
    val recordDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: BookingStatus = BookingStatus.CONFIRMED,

    @Column(name = "booking_date", nullable = false)
    val bookingDate: LocalDate,


    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    var resource: Resource? = null
)


