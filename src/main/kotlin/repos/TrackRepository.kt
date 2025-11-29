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

object TrackRepository {

    private fun resultRowToTrack(row: ResultRow) = Track(
        id = UUID.fromString(row[Tracks.id]),
        name = row[Tracks.name],
        duration = row[Tracks.duration],
        previewUrl = row[Tracks.previewUrl],
        albumId = UUID.fromString(row[Tracks.albumId]),
        artistId = UUID.fromString(row[Tracks.artistId])
    )

    suspend fun create(track: Track): Track = dbQuery {
        Tracks.insert {
            it[id] = track.id.toString()
            it[name] = track.name
            it[duration] = track.duration
            it[previewUrl] = track.previewUrl
            it[albumId] = track.albumId.toString()
            it[artistId] = track.artistId.toString()
        }
        track
    }

    suspend fun findById(id: UUID): Track? = dbQuery {
        Tracks.select { Tracks.id eq id.toString() }
            .map { resultRowToTrack(it) }
            .singleOrNull()
    }

    suspend fun getAll(): List<Track> = dbQuery {
        Tracks.selectAll()
            .map { resultRowToTrack(it) }
    }

    /**
     * Busca canciones por query (nombre de canción, artista o álbum)
     */
    suspend fun search(query: String): List<Track> = dbQuery {
        val q = query.lowercase()

        // Join con Artists y Albums para buscar en sus nombres también
        (Tracks innerJoin Artists innerJoin Albums)
            .select {
                (Tracks.name.lowerCase() like "%$q%") or
                        (Artists.name.lowerCase() like "%$q%") or
                        (Albums.name.lowerCase() like "%$q%")
            }
            .map { resultRowToTrack(it) }
    }
}