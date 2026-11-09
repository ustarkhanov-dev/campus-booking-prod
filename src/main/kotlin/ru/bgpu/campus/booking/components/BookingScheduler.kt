package ru.bgpu.campus.booking.components

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.bgpu.campus.booking.repositories.RecordsRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

@Component
class BookingScheduler(
    private val recordRepository: RecordsRepository,
    private val clock: Clock
) {
    private val log = LoggerFactory.getLogger(BookingScheduler::class.java)

    // Запуск каждые 15 минут
    @Scheduled(fixedRate = 900000)
    @Transactional
    fun autoCompleteExpiredBookings() {
        val today = LocalDate.now(clock)
        val nowTime = LocalTime.now(clock)

        // Один быстрый UPDATE-запрос в БД вместо тяжелого цикла!
        val updatedCount = recordRepository.autoCompleteExpiredBookings(today, nowTime)

        if (updatedCount > 0) {
            log.info("Планировщик: автоматически переведено в COMPLETED записей: $updatedCount")
        }
    }
}