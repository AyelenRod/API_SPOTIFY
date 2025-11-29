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

    suspend fun create(track: Track): Track = dbQuery {
        Tracks.insert {
            it[id] = track.id
            it[name] = track.name
            it[duration] = track.duration
            it[previewUrl] = track.previewUrl
            it[albumId] = track.albumId
            it[artistId] = track.artistId
        }
        track
    }

    suspend fun findById(id: UUID): Track? = dbQuery {
        Tracks.selectAll()
            .where { Tracks.id eq id }
            .map { resultRowToTrack(it) }
            .singleOrNull()
    }

    suspend fun getAll(): List<Track> = dbQuery {
        Tracks.selectAll().map { resultRowToTrack(it) }
    }

    suspend fun search(query: String): List<Track> = dbQuery {
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