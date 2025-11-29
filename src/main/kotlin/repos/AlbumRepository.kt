package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Albums
import com.musicapp.database.Artists
import com.musicapp.database.Tracks
import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import org.jetbrains.exposed.sql.*
import java.util.*

object AlbumRepository {
    private fun resultRowToAlbum(row: ResultRow) = Album(
        id = UUID.fromString(row[Albums.id]),
        name = row[Albums.name],
        artistId = UUID.fromString(row[Albums.artistId]),
        albumArt = row[Albums.albumArt],
        year = row[Albums.year]
    )

    suspend fun create(album: Album): Album = dbQuery {
        Albums.insert {
            it[id] = album.id.toString()
            it[name] = album.name
            it[artistId] = album.artistId.toString()
            it[albumArt] = album.albumArt
            it[year] = album.year
        }
        album
    }

    suspend fun findById(id: UUID): Album? = dbQuery {
        Albums.select { Albums.id eq id.toString() }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    suspend fun getAll(): List<Album> = dbQuery {
        Albums.selectAll()
            .map { resultRowToAlbum(it) }
    }

    suspend fun getAlbumsByArtist(artistId: UUID): List<Album> = dbQuery {
        Albums.select { Albums.artistId eq artistId.toString() }
            .map { resultRowToAlbum(it) }
    }
}
