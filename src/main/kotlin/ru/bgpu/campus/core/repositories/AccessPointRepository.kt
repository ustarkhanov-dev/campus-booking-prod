package ru.bgpu.campus.core.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.bgpu.campus.core.models.AccessPoint


interface AccessPointRepository : JpaRepository<AccessPoint, Long> {

    fun findByExternalId(externalId: Long): AccessPoint?



}