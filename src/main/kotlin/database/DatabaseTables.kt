package com.musicapp.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

// 1. Tabla ARTISTAS
object Artists : Table("artistas") {
    // Error corregido: Quitamos .primaryKey() de aquí
    val id = uuid("id")
    val name = varchar("name", length = 100)
    val genre = varchar("genre", length = 50)

    // Solución: Definimos la Primary Key explícitamente así:
    override val primaryKey = PrimaryKey(id)
}

// 2. Tabla ALBUMES
object Albums : Table("albumes") {
    val id = uuid("id")

    // Mapeo: En Kotlin lo llamas 'name', pero en SQL buscará la columna 'title'
    val name = varchar("title", length = 150)

    // Mapeo: En Kotlin lo llamas 'year', pero en SQL buscará 'release_year'
    val year = integer("release_year")

    // FK con RESTRICT para evitar borrado en cascada (Protección)
    val artistId = uuid("artist_id").references(Artists.id, onDelete = ReferenceOption.RESTRICT)

    // Solución PK
    override val primaryKey = PrimaryKey(id)
}

// 3. Tabla TRACKS
object Tracks : Table("tracks") {
    val id = uuid("id")

    // Mapeo: En Kotlin 'name', en SQL 'title'
    val name = varchar("title", length = 150)

    // En SQL es INTEGER, así que usamos integer (no long)
    val duration = integer("duration")

    // FK con RESTRICT
    val albumId = uuid("album_id").references(Albums.id, onDelete = ReferenceOption.RESTRICT)

    // Solución PK
    override val primaryKey = PrimaryKey(id)
}