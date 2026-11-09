package ru.bgpu.campus.core.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.bgpu.campus.core.models.User
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {


    @Query("SELECT u FROM User u LEFT JOIN FETCH u.allowedAccessPoints WHERE u.username = :username")
    fun findUserByUsernameWithAccessPoints(username: String): User?


    @Query("SELECT u FROM User u WHERE u.username = :username")
    fun findUserByUsername(username: String): User?



}