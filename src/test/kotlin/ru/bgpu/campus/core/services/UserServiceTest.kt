package ru.bgpu.campus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.core.models.AccessPoint
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.repositories.RoomRepository
import ru.bgpu.campus.core.repositories.UserRepository
import java.util.*

class UserServiceTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private val roomRepository: RoomRepository = mockk(relaxed = true)
    private val userService = UserService(userRepository, roomRepository, maxRecordsPerDay = 3)

    @Test
    fun `should link room when student has access to the building (Happy Path)`() {
        val dorm2 = AccessPoint(id = 6L, name = "Общежитие №2")
        val student = User(id = 1L, username = "ivanov", allowedAccessPoints = mutableListOf(dorm2))
        val room204 = Room(id = 42L, number = "204", accessPoint = dorm2)

        every { userRepository.findUserByUsernameWithAccessPoints("ivanov") } returns student
        every { roomRepository.findById(42L) } returns Optional.of(room204)

        val resultNumber = userService.updateUserRoom("ivanov", 42L)

        assertEquals("204", resultNumber)
        assertEquals(room204, student.room)
        verify(exactly = 1) { userRepository.save(student) }
    }

    @Test
    fun `should throw 403 Forbidden when student tries to pick room in foreign building`() {
        val dorm2 = AccessPoint(id = 6L, name = "Общежитие №2")
        val dorm3 = AccessPoint(id = 7L, name = "Общежитие №3")

        // У студента пропуск ТОЛЬКО в Общагу №2
        val student = User(id = 1L, username = "ivanov", allowedAccessPoints = mutableListOf(dorm2))
        // А комната находится в Общаге №3
        val foreignRoom = Room(id = 99L, number = "501", accessPoint = dorm3)

        every { userRepository.findUserByUsernameWithAccessPoints("ivanov") } returns student
        every { roomRepository.findById(99L) } returns Optional.of(foreignRoom)

        val ex = assertThrows<ResponseStatusException> {
            userService.updateUserRoom("ivanov", 99L)
        }
        assertEquals(403, ex.statusCode.value())
    }

    @BeforeEach
    fun setUp() {
        every { userRepository.save(any()) } answers { firstArg() }
    }
}