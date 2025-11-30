package com.musicapp.repos

import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.database.Albums
import com.musicapp.database.Artists
import com.musicapp.database.Tracks
import com.musicapp.models.Track
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    // CREATE
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

    // READ - TRACK POR ID
    suspend fun getTrackById(id: UUID): Track? = dbQuery {
        Tracks.selectAll()
            .where { Tracks.id eq id }
            .map { resultRowToTrack(it) }
            .singleOrNull()
    }

    // READ - TODOS LOS TRACKS
    suspend fun getAllTracks(): List<Track> = dbQuery {
        Tracks.selectAll()
            .map { resultRowToTrack(it) }
    }

    // READ - TRACKS POR ÁLBUM
    suspend fun getTracksByAlbum(albumId: UUID): List<Track> = dbQuery {
        Tracks.selectAll()
            .where { Tracks.albumId eq albumId }
            .map { resultRowToTrack(it) }
    }

    // READ - TRACKS POR ARTISTA
    suspend fun getTracksByArtist(artistId: UUID): List<Track> = dbQuery {
        Tracks.selectAll()
            .where { Tracks.artistId eq artistId }
            .map { resultRowToTrack(it) }
    }

    // READ - BÚSQUEDA DE TRACKS POR NOMBRE, ARTISTA O ÁLBUM
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

    // UPDATE - ACTUALIZA UN TRACK
    suspend fun updateTrack(
        id: UUID,
        name: String? = null,
        duration: Long? = null,
        previewUrl: String? = null
    ): Boolean = dbQuery {
        val updateCount = Tracks.update({ Tracks.id eq id }) {
            name?.let { value -> it[Tracks.name] = value }
            duration?.let { value -> it[Tracks.duration] = value }
            previewUrl?.let { value -> it[Tracks.previewUrl] = value }
        }
        updateCount > 0
    }

    // DELETE
    suspend fun deleteTrack(id: UUID): Boolean = dbQuery {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }
}