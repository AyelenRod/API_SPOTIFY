package com.musicapp.repos

import com.musicapp.database.Albums
import com.musicapp.database.Tracks
import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.models.Album
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

object AlbumRepository {
    private fun resultRowToAlbum(row: ResultRow) = Album(
        id = row[Albums.id],
        name = row[Albums.name],
        artistId = row[Albums.artistId],
        year = row[Albums.year]
    )

    suspend fun createAlbum(name: String, artistId: UUID, year: Int): Album = dbQuery {
        val newId = UUID.randomUUID()
        Albums.insert {
            it[id] = newId
            it[Albums.name] = name
            it[Albums.artistId] = artistId
            it[Albums.year] = year
        }
        Albums.selectAll().where { Albums.id eq newId }
            .map { resultRowToAlbum(it) }
            .single()
    }

    suspend fun getAlbumById(id: UUID): Album? = dbQuery {
        Albums.selectAll().where { Albums.id eq id }
            .map { resultRowToAlbum(it) }
            .singleOrNull()
    }

    suspend fun getAllAlbums(): List<Album> = dbQuery {
        Albums.selectAll().map { resultRowToAlbum(it) }
    }

    suspend fun updateAlbum(id: UUID, name: String? = null, year: Int? = null): Boolean = dbQuery {
        val updateCount = Albums.update({ Albums.id eq id }) {
            name?.let { v -> it[Albums.name] = v }
            year?.let { v -> it[Albums.year] = v }
        }
        updateCount > 0
    }

    suspend fun deleteAlbum(id: UUID): Boolean = dbQuery {
        try {
            Albums.deleteWhere { Albums.id eq id } > 0
        } catch (e: ExposedSQLException) {
            throw IllegalStateException("No se puede borrar el álbum: tiene canciones asociadas.")
        }
    }

    // Metodo auxiliar
    suspend fun hasTracks(id: UUID): Boolean = dbQuery {
        Tracks.selectAll().where { Tracks.albumId eq id }.count() > 0
    }
}