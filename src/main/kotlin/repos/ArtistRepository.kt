package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Artists
import com.musicapp.models.Artist
import org.jetbrains.exposed.sql.*
import java.util.*

object ArtistRepository {

    private fun resultRowToArtist(row: ResultRow) = Artist(
        id = row[Artists.id],
        name = row[Artists.name],
        genre = row[Artists.genre],
        image = row[Artists.image]
    )

    // MODIFICADO: Crea el artista usando los parámetros del servicio
    suspend fun createArtist(name: String, genre: String, imageUrl: String): Artist = dbQuery {
        val newId = UUID.randomUUID()
        Artists.insert {
            it[id] = newId
            it[Artists.name] = name
            it[Artists.genre] = genre
            it[image] = imageUrl // Guarda la URL de S3
        }

        Artists.selectAll()
            .where { Artists.id eq newId }
            .map { resultRowToArtist(it) }
            .single()
    }

    // CORREGIDO: Renombrado a getArtistById
    suspend fun getArtistById(id: UUID): Artist? = dbQuery {
        Artists.selectAll()
            .where { Artists.id eq id }
            .map { resultRowToArtist(it) }
            .singleOrNull()
    }

    // (Funciones findById/create/getAll anteriores eliminadas o renombradas)
}