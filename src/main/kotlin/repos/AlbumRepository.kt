package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Albums
import com.musicapp.models.Album
import org.jetbrains.exposed.sql.*
import java.util.*

object AlbumRepository {

    private fun resultRowToAlbum(row: ResultRow) = Album(
        id = row[Albums.id],
        name = row[Albums.name],
        artistId = row[Albums.artistId],
        albumArt = row[Albums.albumArt],
        year = row[Albums.year]
    )

    // MODIFICADO: Crea el álbum usando los parámetros del servicio
    suspend fun createAlbum(name: String, artistId: UUID, year: Int, albumArtUrl: String): Album = dbQuery {
        val newId = UUID.randomUUID()
        Albums.insert {
            it[id] = newId
            it[Albums.name] = name
            it[Albums.artistId] = artistId
            it[albumArt] = albumArtUrl // Guarda la URL de S3
            it[Albums.year] = year
        }

        Albums.selectAll()
            .where { Albums.id eq newId }
            .map { resultRowToAlbum(it) }
            .single()
    }

    // CORREGIDO: Renombrado a getAlbumById
    suspend fun getAlbumById(id: UUID): Album? = dbQuery {
        Albums.selectAll()
            .where { Albums.id eq id }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    // (Funciones findById/create/getAll anteriores eliminadas o renombradas)
}