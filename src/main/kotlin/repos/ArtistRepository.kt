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

object ArtistRepository {

    private fun resultRowToArtist(row: ResultRow) = Artist(
        id = UUID.fromString(row[Artists.id]),
        name = row[Artists.name],
        genre = row[Artists.genre],
        image = row[Artists.image]
    )

    suspend fun create(artist: Artist): Artist = dbQuery {
        Artists.insert {
            it[id] = artist.id.toString()
            it[name] = artist.name
            it[genre] = artist.genre
            it[image] = artist.image
        }
        artist
    }

    suspend fun findById(id: UUID): Artist? = dbQuery {
        Artists.select { Artists.id eq id.toString() }
            .map { resultRowToArtist(it) }
            .singleOrNull()
    }

    suspend fun getAll(): List<Artist> = dbQuery {
        Artists.selectAll()
            .map { resultRowToArtist(it) }
    }
}