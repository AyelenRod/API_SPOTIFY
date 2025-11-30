package com.musicapp.services

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.TrackRepository
import java.util.*

class ContentService(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) {

    // ARTISTAS

    // CREAR ARTISTA
    suspend fun createArtist(name: String, genre: String): Artist {
        return artistRepository.createArtist(
            name = name,
            genre = genre,
            imageUrl = ""
        )
    }

    // OBTENER ARTISTA POR ID
    suspend fun getArtistById(id: String): Artist? {
        val artistUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for artist ID")
        }
        return artistRepository.getArtistById(artistUUID)
    }

    // OBTENER TODOS LOS ARTISTAS
    suspend fun getAllArtists(): List<Artist> {
        return artistRepository.getAllArtists()
    }

    // ACTUALIZAR ARTISTA
    suspend fun updateArtist(
        id: String,
        name: String? = null,
        genre: String? = null
    ): Artist? {
        val artistUUID = UUID.fromString(id)

        val existingArtist = artistRepository.getArtistById(artistUUID)
            ?: return null

        val updated = artistRepository.updateArtist(
            id = artistUUID,
            name = name,
            genre = genre,
        )

        return if (updated) artistRepository.getArtistById(artistUUID) else null
    }

    // ELIMINAR ARTISTA
    suspend fun deleteArtist(id: String): Boolean {
        val artistUUID = UUID.fromString(id)

        if (artistRepository.hasAlbums(artistUUID)) {
            throw IllegalStateException("Cannot delete artist with albums")
        }
        if (artistRepository.hasTracks(artistUUID)) {
            throw IllegalStateException("Cannot delete artist with tracks")
        }

        return artistRepository.deleteArtist(artistUUID)
    }

    // ALBUMES

    // CREAR ÁLBUM
    suspend fun createAlbum(
        title: String,
        artistId: String,
        releaseYear: Int
    ): Album {
        val artistUUID = UUID.fromString(artistId)

        artistRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist not found")

        return albumRepository.createAlbum(
            name = title,
            artistId = artistUUID,
            year = releaseYear,
            albumArtUrl = ""
        )
    }

    //OBTENER ÁLBUM POR ID
    suspend fun getAlbumById(id: String): Album? {
        val albumUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for album ID")
        }
        return albumRepository.getAlbumById(albumUUID)
    }

    //OBTENER TODOS LOS ÁLBUMES
    suspend fun getAllAlbums(): List<Album> {
        return albumRepository.getAllAlbums()
    }

    // ACTUALIZAR ÁLBUM
    suspend fun updateAlbum(
        id: String,
        title: String? = null,
        releaseYear: Int? = null
    ): Album? {
        val albumUUID = UUID.fromString(id)

        val existingAlbum = albumRepository.getAlbumById(albumUUID)
            ?: return null

        val updated = albumRepository.updateAlbum(
            id = albumUUID,
            name = title,
            year = releaseYear,
        )

        return if (updated) albumRepository.getAlbumById(albumUUID) else null
    }

    // ELIMINAR ÁLBUM
    suspend fun deleteAlbum(id: String): Boolean {
        val albumUUID = UUID.fromString(id)

        if (albumRepository.hasTracks(albumUUID)) {
            throw IllegalStateException("Cannot delete album with tracks")
        }

        return albumRepository.deleteAlbum(albumUUID)
    }

    // TRACKS

    // CREAR TRACK
    suspend fun createTrack(
        title: String,
        albumId: String,
        duration: Long
    ): Track {
        val albumUUID = UUID.fromString(albumId)

        val album = albumRepository.getAlbumById(albumUUID)
            ?: throw IllegalArgumentException("Album not found")

        val artistId = album.artistId

        return trackRepository.createTrack(
            name = title,
            albumId = albumUUID,
            duration = duration,
            artistId = artistId,
            previewUrl = ""
        )
    }

    // OBTENER TRACK POR ID
    suspend fun getTrackById(id: String): Track? {
        val trackUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for track ID")
        }
        return trackRepository.getTrackById(trackUUID)
    }

    // OBTENER TODOS LOS TRACKS
    suspend fun getAllTracks(): List<Track> {
        return trackRepository.getAllTracks()
    }

    // ACTUALIZAR TRACK
    suspend fun updateTrack(
        id: String,
        title: String? = null,
        duration: Long? = null
    ): Track? {
        val trackUUID = UUID.fromString(id)

        val existingTrack = trackRepository.getTrackById(trackUUID)
            ?: return null

        val updated = trackRepository.updateTrack(
            id = trackUUID,
            name = title,
            duration = duration,
        )

        return if (updated) trackRepository.getTrackById(trackUUID) else null
    }

    // ELIMINAR TRACK
    suspend fun deleteTrack(id: String): Boolean {
        val trackUUID = UUID.fromString(id)
        return trackRepository.deleteTrack(trackUUID)
    }
}
