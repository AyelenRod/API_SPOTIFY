package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Artists
import com.musicapp.models.Artist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

object ArtistRepository {
    private fun resultRowToArtist(row: ResultRow) = Artist(
        id = row[Artists.id],
        name = row[Artists.name],
        genre = row[Artists.genre],
        image = row[Artists.image]
    )

    // CREATE
    suspend fun createArtist(name: String, genre: String, imageUrl: String): Artist = dbQuery {
        val newId = UUID.randomUUID()
        Artists.insert {
            it[id] = newId
            it[Artists.name] = name
            it[Artists.genre] = genre
            it[image] = imageUrl
        }

        Artists.selectAll()
            .where { Artists.id eq newId }
            .map { resultRowToArtist(it) }
            .single()
    }

    //READ - ARTISTA POR ID
    suspend fun getArtistById(id: UUID): Artist? = dbQuery {
        Artists.selectAll()
            .where { Artists.id eq id }
            .map { resultRowToArtist(it) }
            .singleOrNull()
    }

    //READ - TODOS LOS ARTISTAS
    suspend fun getAllArtists(): List<Artist> = dbQuery {
        Artists.selectAll()
            .map { resultRowToArtist(it) }
    }

    // UPDATE
    suspend fun updateArtist(
        id: UUID,
        name: String? = null,
        genre: String? = null,
        imageUrl: String? = null
    ): Boolean = dbQuery {
        val updateCount = Artists.update({ Artists.id eq id }) {
            name?.let { value -> it[Artists.name] = value }
            genre?.let { value -> it[Artists.genre] = value }
            imageUrl?.let { value -> it[image] = value }
        }
        updateCount > 0
    }

    // DELETE
    suspend fun deleteArtist(id: UUID): Boolean = dbQuery {
        Artists.deleteWhere { Artists.id eq id } > 0
    }

    // VERIFICAR ALBUMNS ASOCIADOS
    suspend fun hasAlbums(id: UUID): Boolean = dbQuery {
        val count = com.musicapp.database.Albums
            .selectAll()
            .where { com.musicapp.database.Albums.artistId eq id }
            .count()
        count > 0
    }

    // VERIFICAR TRACKS ASOCIADOS
    suspend fun hasTracks(id: UUID): Boolean = dbQuery {
        val count = com.musicapp.database.Tracks
            .selectAll()
            .where { com.musicapp.database.Tracks.artistId eq id }
            .count()
        count > 0
    }
}