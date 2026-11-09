package ru.bgpu.campus.core.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "access_points")
class AccessPoint(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: Long?= null,

    @Column(nullable = false)
    val name: String = " ",

    @Column(name = "short_name")
    val shortName: String = " ",

    @Column(nullable = false)
    var archival: Boolean = false,



    @Column(length = 500)
    var tags: String? = null
) {

}