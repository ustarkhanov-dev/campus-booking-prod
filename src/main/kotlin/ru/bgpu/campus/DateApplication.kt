package ru.bgpu.campus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["ru.bgpu.campus"])
@EnableJpaRepositories(basePackages = ["ru.bgpu.campus"])
@EntityScan(basePackages = ["ru.bgpu.campus"])
class DateApplication

fun main(args: Array<String>) {
	runApplication<ru.bgpu.campus.DateApplication>(*args)
}
