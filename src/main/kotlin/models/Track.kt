<<<<<<< HEAD
package models
=======
package com.musicapp.models
>>>>>>> api2

import java.util.*

data class Track(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val duration: Long,
    val previewUrl: String,
    val albumId: UUID,
    val artistId: UUID
)
