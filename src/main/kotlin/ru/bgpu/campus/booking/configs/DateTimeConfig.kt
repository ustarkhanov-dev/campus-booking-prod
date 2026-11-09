package ru.bgpu.campus.booking.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Clock
import java.time.ZoneId

@Configuration
@EnableScheduling
class DateTimeConfig {

    @Bean
    fun clock(): Clock {
        // Фиксируем часовой пояс Благовещенска (UTC+9)
        return Clock.system(ZoneId.of("Asia/Yakutsk"))
    }
}
