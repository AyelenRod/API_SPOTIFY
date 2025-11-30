package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Artists
import com.musicapp.database.Albums
import com.musicapp.models.Artist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

object ArtistRepository {
    private fun resultRowToArtist(row: ResultRow) = Artist(
        id = row[Artists.id],
        name = row[Artists.name],
        genre = row[Artists.genre]
    )

    suspend fun createArtist(name: String, genre: String): Artist = dbQuery {
        val newId = UUID.randomUUID()
        Artists.insert {
            it[id] = newId
            it[Artists.name] = name
            it[Artists.genre] = genre
        }
        Artists.selectAll().where { Artists.id eq newId }
            .map { resultRowToArtist(it) }
            .single()
    }

    suspend fun getArtistById(id: UUID): Artist? = dbQuery {
        Artists.selectAll().where { Artists.id eq id }
            .map { resultRowToArtist(it) }
            .singleOrNull()
    }

    suspend fun getAllArtists(): List<Artist> = dbQuery {
        Artists.selectAll().map { resultRowToArtist(it) }
    }

    suspend fun updateArtist(id: UUID, name: String? = null, genre: String? = null): Boolean = dbQuery {
        val updateCount = Artists.update({ Artists.id eq id }) {
            name?.let { v -> it[Artists.name] = v }
            genre?.let { v -> it[Artists.genre] = v }
        }
        updateCount > 0
    }

    suspend fun deleteArtist(id: UUID): Boolean = dbQuery {
        try {
            Artists.deleteWhere { Artists.id eq id } > 0
        } catch (e: ExposedSQLException) {
            throw IllegalStateException("No se puede borrar el artista: tiene álbumes asociados.")
        }
    }

    suspend fun hasAlbums(id: UUID): Boolean = dbQuery {
        Albums.selectAll().where { Albums.artistId eq id }.count() > 0
    }
}