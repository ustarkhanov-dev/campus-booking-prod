package ru.bgpu.campus.core.models

import jakarta.persistence.*

enum class RoomType {
    RESIDENTIAL,
    COMMON
}

@Entity
@Table(name = "rooms")
class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = true)
    val number: String? = null,

    @Column(nullable = true)
    val name: String? = null,

    @Column(nullable = false)
    val floor: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: RoomType = RoomType.RESIDENTIAL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_point_id", nullable = false)
    val accessPoint: AccessPoint? = null
) {


}