package ru.bgpu.campus.core.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.bgpu.campus.core.models.Room
import ru.bgpu.campus.core.models.RoomType


@Repository
interface RoomRepository : JpaRepository<Room, Long> {


    fun findAllByAccessPointIdOrderByFloorAscNumberAsc(accessPointId: Long): List<Room>
    fun findAllByAccessPointIdAndTypeOrderByFloorAscNumberAsc(accessPointId: Long, type: RoomType): List<Room>
    fun existsByNumberAndAccessPointId(number: String, accessPointId: Long): Boolean

    }
