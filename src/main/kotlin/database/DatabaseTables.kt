package com.musicapp.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object Artists : Table("artistas") {
    val id = uuid("id")
    val name = varchar("name", length = 100)
    val genre = varchar("genre", length = 50)

    override val primaryKey = PrimaryKey(id)
}

object Albums : Table("albumes") {
    val id = uuid("id")
    val name = varchar("title", length = 150)
    val year = integer("release_year")
    val artistId = uuid("artist_id").references(Artists.id, onDelete = ReferenceOption.RESTRICT)

    override val primaryKey = PrimaryKey(id)
}

object Tracks : Table("tracks") {
    val id = uuid("id")
    val name = varchar("title", length = 150)
    val duration = integer("duration")
    val albumId = uuid("album_id").references(Albums.id, onDelete = ReferenceOption.RESTRICT)

    override val primaryKey = PrimaryKey(id)
}