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

    // Crea un nuevo álbum en la base de datos.
    suspend fun createAlbum(name: String, artistId: UUID, year: Int, albumArtUrl: String): Album = dbQuery {
        val newId = UUID.randomUUID()
        Albums.insert {
            it[id] = newId
            it[Albums.name] = name
            it[Albums.artistId] = artistId
            it[albumArt] = albumArtUrl
            it[Albums.year] = year
        }

        Albums.selectAll()
            .where { Albums.id eq newId }
            .map { resultRowToAlbum(it) }
            .single()
    }

    // Obtiene un álbum por su ID.
    suspend fun getAlbumById(id: UUID): Album? = dbQuery {
        Albums.selectAll()
            .where { Albums.id eq id }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    // Obtiene todos los álbumes de la base de datos.
    suspend fun getAllAlbums(): List<Album> = dbQuery {
        Albums.selectAll()
            .map { resultRowToAlbum(it) }
    }
}