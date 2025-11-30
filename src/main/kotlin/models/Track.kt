package com.musicapp.models
import java.util.UUID

data class Track(
    val id: UUID,
    val name: String,
    val albumId: UUID,
    val duration: Long
)