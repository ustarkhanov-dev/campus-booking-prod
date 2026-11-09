package ru.bgpu.campus.core.services


import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.bgpu.campus.core.models.User
import ru.bgpu.campus.core.repositories.RoomRepository
import ru.bgpu.campus.core.repositories.UserRepository


@Service
 class UserService(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,

    @Value("\${booking.max-records-per-day:3}")
    private var maxRecordsPerDay: Int
) {


    fun findAll() : List<User> {
        return userRepository.findAll()
    }

    fun getMaxRecordsPerDay(): Int = maxRecordsPerDay


    @Transactional
    fun updateUserRoom(username: String, roomId: Long): String {

        val user = userRepository.findUserByUsernameWithAccessPoints(username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")


        val room = roomRepository.findById(roomId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Комната с ID $roomId не найдена")
        }


        val roomAccessPointId = room.accessPoint?.id
        val hasAccessToBuilding = user.allowedAccessPoints.any { it.id == roomAccessPointId }

        if (!hasAccessToBuilding) {
            val buildingName = room.accessPoint?.name ?: "неизвестный корпус"
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Вы не можете выбрать комнату в '$buildingName', так как у вас нет пропуска в это здание"
            )
        }


        user.room = room
        userRepository.save(user)

        return user.room?.number ?: ""
    }




    fun findUserByUsername(username: String): User? = userRepository.findUserByUsername(username)

}