<<<<<<< HEAD
package models
=======
package com.musicapp.models
>>>>>>> api2

import java.util.*

data class Album(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val artistId: UUID,
    val albumArt: String,
    val year: Int
)
