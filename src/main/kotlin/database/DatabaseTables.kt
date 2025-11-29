package com.musicapp.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.KotlinLocalDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.default
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone
import java.time.LocalDateTime

// Importamos la extensión Exposed UUID que se usa internamente si usas exposed-dao
// aunque la versión 0.46.0 la maneja, debemos usar el tipo 'uuid'
import org.jetbrains.exposed.sql.kotlin.datetime.KotlinLocalDateTime.now
import java.util.UUID // Necesario para definir las columnas de UUID

object Users : Table("users") {
    val id = uuid("id").uniqueIndex() // CORRECCIÓN: Usar tipo UUID de Exposed
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 100)
    val role = varchar("role", 20).default("USER")
    val createdAt = datetime("created_at").default(LocalDateTime.now()) // CORRECCIÓN: Usar default con java.time.LocalDateTime
    override val primaryKey = PrimaryKey(id)
}

object Artists : Table("artists") {
    val id = uuid("id").uniqueIndex() // CORRECCIÓN
    val name = varchar("name", 200)
    val genre = varchar("genre", 100)
    val image = text("image")
    val createdAt = datetime("created_at").default(LocalDateTime.now()) // CORRECCIÓN
    override val primaryKey = PrimaryKey(id)
}

object Albums : Table("albums") {
    val id = uuid("id").uniqueIndex() // CORRECCIÓN
    val name = varchar("name", 200)
    val artistId = uuid("artist_id").references(Artists.id) // CORRECCIÓN: Referencia a UUID
    val albumArt = text("album_art")
    val year = integer("year")
    val createdAt = datetime("created_at").default(LocalDateTime.now()) // CORRECCIÓN
    override val primaryKey = PrimaryKey(id)
}

object Tracks : Table("tracks") {
    val id = uuid("id").uniqueIndex() // CORRECCIÓN
    val name = varchar("name", 200)
    val duration = long("duration")
    val previewUrl = text("preview_url")
    val albumId = uuid("album_id").references(Albums.id) // CORRECCIÓN: Referencia a UUID
    val artistId = uuid("artist_id").references(Artists.id) // CORRECCIÓN: Referencia a UUID
    val createdAt = datetime("created_at").default(LocalDateTime.now()) // CORRECCIÓN
    override val primaryKey = PrimaryKey(id)
}