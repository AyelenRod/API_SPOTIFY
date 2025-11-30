package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Albums
import com.musicapp.models.Album
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

object AlbumRepository {
    private fun resultRowToAlbum(row: ResultRow) = Album(
        id = row[Albums.id],
        name = row[Albums.name],
        artistId = row[Albums.artistId],
        albumArt = row[Albums.albumArt],
        year = row[Albums.year]
    )

    // CREATE
    suspend fun createAlbum(
        name: String,
        artistId: UUID,
        year: Int,
        albumArtUrl: String
    ): Album = dbQuery {
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

    // READ - ALBÚM POR ID
    suspend fun getAlbumById(id: UUID): Album? = dbQuery {
        Albums.selectAll()
            .where { Albums.id eq id }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    // READ - TODOS LOS ÁLBUMES
    suspend fun getAllAlbums(): List<Album> = dbQuery {
        Albums.selectAll()
            .map { resultRowToAlbum(it) }
    }

    // READ
    suspend fun getAlbumsByArtist(artistId: UUID): List<Album> = dbQuery {
        Albums.selectAll()
            .where { Albums.artistId eq artistId }
            .map { resultRowToAlbum(it) }
    }

    // UPDATE
    suspend fun updateAlbum(
        id: UUID,
        name: String? = null,
        year: Int? = null,
        albumArtUrl: String? = null
    ): Boolean = dbQuery {
        val updateCount = Albums.update({ Albums.id eq id }) {
            name?.let { value -> it[Albums.name] = value }
            year?.let { value -> it[Albums.year] = value }
            albumArtUrl?.let { value -> it[albumArt] = value }
        }
        updateCount > 0
    }

    // DELETE
    suspend fun deleteAlbum(id: UUID): Boolean = dbQuery {
        Albums.deleteWhere { Albums.id eq id } > 0
    }

    // VERIFICAR TRACKS ASOCIADOS
    suspend fun hasTracks(id: UUID): Boolean = dbQuery {
        val count = com.musicapp.database.Tracks
            .selectAll()
            .where { com.musicapp.database.Tracks.albumId eq id }
            .count()
        count > 0
    }
}