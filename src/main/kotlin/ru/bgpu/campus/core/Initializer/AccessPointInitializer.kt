package ru.bgpu.campus.core.Initializer

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import ru.bgpu.campus.core.models.AccessPoint
import ru.bgpu.campus.core.repositories.AccessPointRepository

@Component
@Order(1)  // Выполняется первым
class AccessPointInitializer @Autowired constructor(
    private val accessPointRepository: AccessPointRepository
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (accessPointRepository.count() > 0) {
            println("✓ Точки доступа уже существуют (${accessPointRepository.count()} записей)")
            return
        }

        val points = listOf(
            AccessPoint(externalId = 1, name = "Центральный корпус", shortName = "Ц"),
            AccessPoint(externalId = 2, name = "Корпус А", shortName = "А"),
            AccessPoint(externalId = 3, name = "Корпус Б", shortName = "Б"),
            AccessPoint(externalId = 4, name = "Корпус В", shortName = "В"),
            AccessPoint(externalId = 5, name = "Корпус Г", shortName = "Г"),
            AccessPoint(externalId = 6, name = "Общежитие №2", shortName = "2"),
            AccessPoint(externalId = 7, name = "Общежитие №3", shortName = "3"),
            AccessPoint(externalId = 8, name = "Общежитие №4", shortName = "4"),
            AccessPoint(externalId = 9, name = "Общежитие №5", shortName = "5")
        )

        accessPointRepository.saveAll(points)
        println("✓ Инициализировано точек доступа: ${points.size}")
    }
}