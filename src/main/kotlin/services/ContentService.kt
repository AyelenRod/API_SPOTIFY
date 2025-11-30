package com.musicapp.services

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.TrackRepository
import java.util.UUID

class ContentService(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) {

    //ARTISTAS
    suspend fun createArtist(name: String, genre: String): Artist {
        return artistRepository.createArtist(name = name, genre = genre)
    }

    suspend fun getArtistById(id: String): Artist? {
        val artistUUID = parseUUID(id) ?: throw IllegalArgumentException("ID de artista inválido")
        return artistRepository.getArtistById(artistUUID)
    }

    suspend fun getAllArtists(): List<Artist> = artistRepository.getAllArtists()

    suspend fun updateArtist(id: String, name: String? = null, genre: String? = null): Artist? {
        val artistUUID = parseUUID(id) ?: return null

        artistRepository.getArtistById(artistUUID) ?: return null

        val updated = artistRepository.updateArtist(id = artistUUID, name = name, genre = genre)
        return if (updated) artistRepository.getArtistById(artistUUID) else null
    }

    suspend fun deleteArtist(id: String): Boolean {
        val artistUUID = parseUUID(id) ?: return false

        // PROTECCIÓN CONTRA BORRADO EN CASCADA
        if (artistRepository.hasAlbums(artistUUID)) {
            throw IllegalStateException("No se puede eliminar el artista porque tiene álbumes asociados.")
        }

        return artistRepository.deleteArtist(artistUUID)
    }

   //ALBUMS
    suspend fun createAlbum(title: String, artistId: String, releaseYear: Int): Album {
        val artistUUID = parseUUID(artistId) ?: throw IllegalArgumentException("ID de artista inválido")

        if (artistRepository.getArtistById(artistUUID) == null) {
            throw IllegalArgumentException("El artista especificado no existe")
        }

        return albumRepository.createAlbum(name = title, artistId = artistUUID, year = releaseYear)
    }

    suspend fun getAlbumById(id: String): Album? {
        val albumUUID = parseUUID(id) ?: throw IllegalArgumentException("ID de álbum inválido")
        return albumRepository.getAlbumById(albumUUID)
    }

    suspend fun getAllAlbums(): List<Album> = albumRepository.getAllAlbums()

    suspend fun updateAlbum(id: String, title: String? = null, releaseYear: Int? = null): Album? {
        val albumUUID = parseUUID(id) ?: return null

        albumRepository.getAlbumById(albumUUID) ?: return null

        val updated = albumRepository.updateAlbum(id = albumUUID, name = title, year = releaseYear)
        return if (updated) albumRepository.getAlbumById(albumUUID) else null
    }

    suspend fun deleteAlbum(id: String): Boolean {
        val albumUUID = parseUUID(id) ?: return false

        // PROTECCIÓN CONTRA BORRADO EN CASCADA
        if (albumRepository.hasTracks(albumUUID)) {
            throw IllegalStateException("No se puede eliminar el álbum porque tiene canciones asociadas.")
        }

        return albumRepository.deleteAlbum(albumUUID)
    }

    //TRACKS
    suspend fun createTrack(title: String, albumId: String, duration: Long): Track {
        val albumUUID = parseUUID(albumId) ?: throw IllegalArgumentException("ID de álbum inválido")

        if (albumRepository.getAlbumById(albumUUID) == null) {
            throw IllegalArgumentException("El álbum especificado no existe")
        }

        return trackRepository.createTrack(name = title, albumId = albumUUID, duration = duration)
    }

    suspend fun getTrackById(id: String): Track? {
        val trackUUID = parseUUID(id) ?: throw IllegalArgumentException("ID de track inválido")
        return trackRepository.getTrackById(trackUUID)
    }

    suspend fun getAllTracks(): List<Track> = trackRepository.getAllTracks()

    suspend fun updateTrack(id: String, title: String? = null, duration: Long? = null): Track? {
        val trackUUID = parseUUID(id) ?: return null

        trackRepository.getTrackById(trackUUID) ?: return null

        val updated = trackRepository.updateTrack(id = trackUUID, name = title, duration = duration)
        return if (updated) trackRepository.getTrackById(trackUUID) else null
    }

    suspend fun deleteTrack(id: String): Boolean {
        val trackUUID = parseUUID(id) ?: return false
        return trackRepository.deleteTrack(trackUUID)
    }

    // UTILS
    private fun parseUUID(id: String): UUID? {
        return try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}