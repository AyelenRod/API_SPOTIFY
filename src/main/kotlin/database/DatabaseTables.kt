package com.musicapp.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : Table("users") {
    val id = varchar("id", 36).uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val role = varchar("role", 20).default("USER")

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

//tabla de artistas
object Artists : Table("artists") {
    val id = varchar("id", 36).uniqueIndex()
    val name = varchar("name", 200)
    val genre = varchar("genre", 100)
    val image = text("image")  // URL de S3
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

// Tabla de Álbumes
object Albums : Table("albums") {
    val id = varchar("id", 36).uniqueIndex()
    val name = varchar("name", 200)

    val artistId = varchar("artist_id", 36).references(Artists.id)

    val albumArt = text("album_art")  // URL de S3
    val year = integer("year")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}

// Tabla de Pistas
object Tracks : Table("tracks") {
    val id = varchar("id", 36).uniqueIndex()
    val name = varchar("name", 200)
    val duration = long("duration")
    val previewUrl = text("preview_url")

    val albumId = varchar("album_id", 36).references(Albums.id)
    val artistId = varchar("artist_id", 36).references(Artists.id)

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}