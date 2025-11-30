package com.musicapp.models

import java.util.UUID

data class Album(
    val id: UUID,
    val name: String,
    val artistId: UUID,
    val year: Int
)
