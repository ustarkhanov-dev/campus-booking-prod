package ru.bgpu.campus.core.dto

data class UserAccessDto(
    var id: Long?,
    var currentType: Int?,
    var name: String?,
    var shortName: String?,
    var categoryAccess: List<AccessDto>,
    var userAccess: List<AccessDto>
)