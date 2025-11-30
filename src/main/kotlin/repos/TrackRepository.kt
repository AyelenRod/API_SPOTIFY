package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Albums
import com.musicapp.database.Artists
import com.musicapp.database.Tracks
import com.musicapp.models.Track
import org.jetbrains.exposed.sql.*
import java.util.*

object TrackRepository {

    private fun resultRowToTrack(row: ResultRow) = Track(
        id = row[Tracks.id],
        name = row[Tracks.name],
        duration = row[Tracks.duration],
        previewUrl = row[Tracks.previewUrl],
        albumId = row[Tracks.albumId],
        artistId = row[Tracks.artistId]
    )

    // Crea un nuevo track en la base de datos.
    suspend fun createTrack(
        name: String,
        albumId: UUID,
        duration: Long,
        artistId: UUID,
        previewUrl: String
    ): Track = dbQuery {
        val newId = UUID.randomUUID()
        Tracks.insert {
            it[id] = newId
            it[Tracks.name] = name
            it[Tracks.duration] = duration
            it[Tracks.previewUrl] = previewUrl
            it[Tracks.albumId] = albumId
            it[Tracks.artistId] = artistId
        }

        Tracks.selectAll()
            .where { Tracks.id eq newId }
            .map { resultRowToTrack(it) }
            .single()
    }

    // Obtiene un track por su ID.
    suspend fun getTrackById(id: UUID): Track? = dbQuery {
        Tracks.selectAll()
            .where { Tracks.id eq id }
            .map { resultRowToTrack(it) }
            .singleOrNull()
    }

    // Obtiene todos los tracks de la base de datos.
    suspend fun getAllTracks(): List<Track> = dbQuery {
        Tracks.selectAll()
            .map { resultRowToTrack(it) }
    }

    // Busca tracks por nombre, artista o álbum.
    suspend fun searchTracksByName(query: String): List<Track> = dbQuery {
        val q = query.lowercase()
        (Tracks innerJoin Artists innerJoin Albums)
            .selectAll()
            .where {
                (Tracks.name.lowerCase() like "%$q%") or
                        (Artists.name.lowerCase() like "%$q%") or
                        (Albums.name.lowerCase() like "%$q%")
            }
            .map { resultRowToTrack(it) }
    }
}