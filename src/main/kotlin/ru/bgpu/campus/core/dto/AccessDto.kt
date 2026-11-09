package ru.bgpu.campus.core.dto

import java.util.Date


data class AccessDto(
    var id: Long?,
    var active: Boolean?,
    var beginDate: Date?,
    var endDate: Date?,
    var accessPointId: Long?,
    var accessPointName: String?,
    var accessPointShortName: String?
)