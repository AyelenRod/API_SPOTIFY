package com.musicapp.repos

import com.musicapp.database.Tracks
import com.musicapp.database.DatabaseFactory.dbQuery
import com.musicapp.models.Track
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

object TrackRepository {
    private fun resultRowToTrack(row: ResultRow) = Track(
        id = row[Tracks.id],
        name = row[Tracks.name],
        duration = row[Tracks.duration].toLong(),
        albumId = row[Tracks.albumId]
    )
    // Eliminado artistId de los parámetros
    suspend fun createTrack(name: String, albumId: UUID, duration: Long): Track = dbQuery {
        val newId = UUID.randomUUID()
        Tracks.insert {
            it[id] = newId
            it[Tracks.name] = name
            it[Tracks.albumId] = albumId
            it[Tracks.duration] = duration.toInt()
        }
        Tracks.select { Tracks.id eq newId }
            .map { resultRowToTrack(it) }
            .single()
    }

    suspend fun getTrackById(id: UUID): Track? = dbQuery {
        Tracks.select { Tracks.id eq id }.map { resultRowToTrack(it) }.singleOrNull()
    }

    suspend fun getAllTracks(): List<Track> = dbQuery {
        Tracks.selectAll().map { resultRowToTrack(it) }
    }

    suspend fun updateTrack(
        id: UUID,
        name: String? = null,
        duration: Long? = null
    ): Boolean = dbQuery {
        val updateCount = Tracks.update({ Tracks.id eq id }) {
            name?.let { v -> it[Tracks.name] = v }
            duration?.let { v -> it[Tracks.duration] = v.toInt() }
        }
        updateCount > 0
    }

    suspend fun deleteTrack(id: UUID): Boolean = dbQuery {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }
}