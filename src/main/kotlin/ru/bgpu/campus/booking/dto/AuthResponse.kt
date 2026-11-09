package ru.bgpu.campus.booking.dto

data class AuthResponse(
    val token: String,
    val username: String,
    val role: String = "USER"
)
