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

    // MODIFICADO: Crea la pista usando los parámetros del servicio
    suspend fun createTrack(name: String, albumId: UUID, duration: Long, artistId: UUID, previewUrl: String): Track = dbQuery {
        val newId = UUID.randomUUID()
        Tracks.insert {
            it[id] = newId
            it[Tracks.name] = name
            it[Tracks.duration] = duration
            it[Tracks.previewUrl] = previewUrl // Guarda la URL de S3
            it[Tracks.albumId] = albumId
            it[Tracks.artistId] = artistId
        }

        Tracks.selectAll()
            .where { Tracks.id eq newId }
            .map { resultRowToTrack(it) }
            .single()
    }

    // CORREGIDO: Renombrado a getTrackById
    suspend fun getTrackById(id: UUID): Track? = dbQuery {
        Tracks.selectAll()
            .where { Tracks.id eq id }
            .map { resultRowToTrack(it) }
            .singleOrNull()
    }

    // CORREGIDO: Renombrado a searchTracksByName (aunque la implementación busca en 3 tablas)
    suspend fun searchTracksByName(query: String): List<Track> = dbQuery {
        val q = query.lowercase()
        // Asegúrate de que Artists y Albums estén en scope para el innerJoin
        (Tracks innerJoin Artists innerJoin Albums)
            .selectAll()
            .where {
                (Tracks.name.lowerCase() like "%$q%") or
                        (Artists.name.lowerCase() like "%$q%") or
                        (Albums.name.lowerCase() like "%$q%")
            }
            .map { resultRowToTrack(it) }
    }

    // (Funciones findById/create/getAll anteriores eliminadas o renombradas)
}