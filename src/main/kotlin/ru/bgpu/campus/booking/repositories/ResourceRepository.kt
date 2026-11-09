package ru.bgpu.campus.booking.repositories


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import ru.bgpu.campus.booking.models.Resource


@Repository
interface ResourceRepository : JpaRepository<Resource, Long> {




    fun findAllByIsDeletedFalse(): List<Resource>


    fun findAllByAccessPointIdAndIsDeletedFalse(accessPointId: Long): List<Resource>


    fun findAllByAccessPointIdInAndIsDeletedFalse(accessPointIds: List<Long>): List<Resource>

    fun existsByRoomId(roomId: Long): Boolean


}