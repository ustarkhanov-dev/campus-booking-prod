package ru.bgpu.campus.booking.configs

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    // Вкладка 1: ВСЕ пользовательские эндпоинты (из Booking, Core и др.)
    @Bean
    fun userApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("1. Клиентское API (Пользователи)")
            .packagesToScan("ru.bgpu.campus") // Сканируем весь проект
            .pathsToExclude("/**/admin/**")   // Но убираем админские пути
            .build()
    }

    // Вкладка 2: ВСЕ админские эндпоинты (из Booking, Core и др.)
    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("2. Панель управления (Админы)")
            .packagesToScan("ru.bgpu.campus") // Сканируем весь проект
            .pathsToMatch("/**/admin/**")    // Берем только пути с меткой admin
            .build()
    }
}
