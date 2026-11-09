package ru.bgpu.campus.booking.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import ru.bgpu.campus.core.models.AccessPoint
import ru.bgpu.campus.core.models.Room
import java.time.LocalTime


enum class ResourceAccessType {

    CAMPUS_DORM_COMMON,


    FACULTY_RESTRICTED
}

@Entity
@Table(name = "resources")
class Resource(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var isAvailable: Boolean = true,


    @Column(name = "active_from", nullable = false)
    var activeFrom: LocalTime = LocalTime.of(0, 0),

    @Column(name = "active_to", nullable = false)
    var activeTo: LocalTime = LocalTime.of(23, 59),


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_point_id", nullable = false)
    var accessPoint: AccessPoint? = null,


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    var room: Room,


    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    var accessType: ResourceAccessType = ResourceAccessType.CAMPUS_DORM_COMMON,


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "resource_allowed_faculties",
        joinColumns = [JoinColumn(name = "resource_id")]
    )
    @Column(name = "faculty_id")
    var allowedFacultyIds: Set<Long> = emptySet(),

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,


    @Column(columnDefinition = "TEXT")
    var rules: String? = null,


    @JsonIgnore
    @OneToMany(
        mappedBy = "resource",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val records: MutableList<Records> = mutableListOf()
) {


}

