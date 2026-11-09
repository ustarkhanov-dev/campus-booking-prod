package ru.bgpu.campus.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var role: String = "USER",

    @Column(unique = true, nullable = false)
    var username: String = "",



    var surname: String = "",
    var name: String = "",
    var patronymic: String? = null,
    var description: String? = null,
    var groups: String = "",
    @Column(name = "last_synced_at")
    var lastSyncedAt: LocalDateTime? = LocalDateTime.now(),


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    var room: Room? = null,

    @ManyToMany
    @JoinTable(
        name = "user_access_points",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "access_point_id")]
    )
    var allowedAccessPoints: MutableList<AccessPoint> = mutableListOf()
)