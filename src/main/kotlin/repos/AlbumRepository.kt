package com.musicapp.repos

import com.musicapp.database.Albums
import com.musicapp.database.Tracks // Necesario para verificar hijos
import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.models.Album
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

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
            it[Albums.name] = name // Recuerda: Table usa 'title' internamente
            it[Albums.artistId] = artistId
            it[Albums.year] = year
        }
        Albums.select { Albums.id eq newId }
            .map { resultRowToAlbum(it) }
            .single()
    }

    suspend fun getAlbumById(id: UUID): Album? = dbQuery {
        Albums.select { Albums.id eq id }.map { resultRowToAlbum(it) }.singleOrNull()
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
        Albums.deleteWhere { Albums.id eq id } > 0
    }

    // VALIDACIÓN PARA PROTECCIÓN DE BORRADO
    suspend fun hasTracks(id: UUID): Boolean = dbQuery {
        Tracks.select { Tracks.albumId eq id }.count() > 0
    }
}