package com.musicapp.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object Users : Table("users") {
    val id = uuid("id").uniqueIndex()
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val role = varchar("role", 20).default("USER")

    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    override val primaryKey = PrimaryKey(id)
}

object Artists : Table("artists") {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 200)
    val genre = varchar("genre", 100)
    val image = text("image")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(id)
}

object Albums : Table("albums") {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 200)
    val artistId = uuid("artist_id").references(Artists.id)
    val albumArt = text("album_art")
    val year = integer("year")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(id)
}

object Tracks : Table("tracks") {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 200)
    val duration = long("duration")
    val previewUrl = text("preview_url")
    val albumId = uuid("album_id").references(Albums.id)
    val artistId = uuid("artist_id").references(Artists.id)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(id)
}