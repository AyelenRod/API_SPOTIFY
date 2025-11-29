package com.musicapp.models

import java.util.*

data class Artist(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val genre: String,
    val image: String
)
