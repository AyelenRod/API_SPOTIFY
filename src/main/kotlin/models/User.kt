package com.musicapp.models

import java.util.*

data class User(
    val id: UUID = UUID.randomUUID(),
    val username: String,
    val password: String,
    val role: String = "USER"
)

data class UserResponse(
    val id: String,
    val username: String,
    val role: String
)

fun User.toResponse() = UserResponse(
    id = id.toString(),
    username = username,
    role = role
)