package com.musicapp.models

import java.util.*

data class Album(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val artistId: UUID,
    val albumArt: String,
    val year: Int
)
