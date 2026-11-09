package ru.bgpu.campus.booking.controllers

import io.restassured.http.ContentType
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import ru.bgpu.campus.booking.dto.CreateRecordRequest
import ru.bgpu.campus.booking.models.BookingStatus
import ru.bgpu.campus.booking.models.Records
import ru.bgpu.campus.booking.models.Resource
import ru.bgpu.campus.booking.services.RecordService
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.User
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
])
class RecordApiTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var recordService: RecordService

    @BeforeEach
    fun setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc)
    }

    @Test
    @WithMockUser(username = "ivanov")
    @DisplayName("POST /record: успешное создание бронирования возвращает 200 и валидный JSON")
    fun `createRecord should return 200 and correct json response`() {
        val dummyRequest = CreateRecordRequest(1L, LocalDate.of(2026, 11, 1), LocalTime.of(10, 0), LocalTime.of(12, 0))
        val dummyRecord = Records(
            id = 42L,
            bookingDate = LocalDate.of(2026, 11, 1),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(12, 0),
            status = BookingStatus.CONFIRMED,
            user = User(username = "ivanov"),
            resource = Resource(name = "Стиралка №1", room = Room(id = 5L))
        )

        given(recordService.createRecord(any() ?: dummyRequest, eq("ivanov") ?: ""))
            .willReturn(dummyRecord)

        val requestBody = """
            {
                "resourceId": 1,
                "bookingDate": "2026-11-01",
                "startTime": "10:00:00",
                "endTime": "12:00:00"
            }
        """.trimIndent()

        RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post("/record")
            .then()
            .statusCode(200)
            .body("id", equalTo(42))
            .body("username", equalTo("ivanov"))
            .body("resourceName", equalTo("Стиралка №1"))
            .body("status", equalTo("CONFIRMED"))
            .body("roomId", equalTo(5))
    }

    @Test
    @WithMockUser(username = "ivanov")
    @DisplayName("POST /record: валидация DTO отсекает неполный запрос со статусом 400")
    fun `createRecord should return 400 when required fields are missing`() {
        val invalidRequestBody = """
            {
                "resourceId": null
            }
        """.trimIndent()

        RestAssuredMockMvc.given()
            .contentType(ContentType.JSON)
            .body(invalidRequestBody)
            .post("/record")
            .then()
            .statusCode(400)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/record/my", "/admin/rooms", "/users/me"])
    @DisplayName("Security: закрытые эндпоинты блокируют неавторизованные запросы")
    fun `secured endpoints should return 403 when unauthorized`(endpoint: String) {
        RestAssuredMockMvc.given()
            .get(endpoint)
            .then()
            .statusCode(`is`(403))
    }
}