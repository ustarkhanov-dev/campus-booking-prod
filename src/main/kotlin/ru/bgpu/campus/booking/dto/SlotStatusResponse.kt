package ru.bgpu.campus.booking.dto

data class SlotStatusResponse(
    val id: Long?,
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean,
    val resourceName: String?,
    // Вместо бесконечной сущности Records, пишем краткий список ID или имен тех, кто забронировал
    val activeBookings: List<Long> = emptyList()   // ФИО или username студента (null, если свободен)
)
