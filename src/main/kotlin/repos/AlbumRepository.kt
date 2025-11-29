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

    suspend fun create(album: Album): Album = dbQuery {
        Albums.insert {
            it[id] = album.id
            it[name] = album.name
            it[artistId] = album.artistId
            it[albumArt] = album.albumArt
            it[year] = album.year
        }
        album
    }

    suspend fun findById(id: UUID): Album? = dbQuery {
        Albums.selectAll()
            .where { Albums.id eq id }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    suspend fun getAll(): List<Album> = dbQuery {
        Albums.selectAll().map { resultRowToAlbum(it) }
    }
}