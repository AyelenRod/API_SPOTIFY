package com.musicapp.services

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.models.TrackDTO
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.TrackRepository
import java.util.*

class ContentService(
    private val s3Service: S3Service,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) {

    // Genera una clave única para un archivo en S3
    private fun generateFileKey(originalFileName: String, folder: String): String {
        val extension = originalFileName.substringAfterLast('.', "")
        val uniqueId = UUID.randomUUID().toString()
        return "$folder/$uniqueId.$extension"
    }

    // Convierte un Track a TrackDTO, incluyendo datos de artista y álbum
    private suspend fun toTrackDTO(track: Track): TrackDTO {
        val artist = artistRepository.getArtistById(track.artistId)
            ?: throw IllegalStateException("Artist not found for track ${track.id}")

        val album = albumRepository.getAlbumById(track.albumId)
            ?: throw IllegalStateException("Album not found for track ${track.id}")

        return TrackDTO(
            id = track.id.toString(),
            name = track.name,
            artist = artist.name,
            album = album.name,
            albumArt = album.albumArt,
            duration = track.duration,
            previewUrl = track.previewUrl
        )
    }

    // MÉTODOS GET (LECTURA)
    suspend fun getArtistById(id: String): Artist? {
        val artistUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for artist ID")
        }
        return artistRepository.getArtistById(artistUUID)
    }

    suspend fun getAllArtists(): List<Artist> {
        return artistRepository.getAllArtists()
    }

    suspend fun getAlbumById(id: String): Album? {
        val albumUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for album ID")
        }
        return albumRepository.getAlbumById(albumUUID)
    }

    suspend fun getAllAlbums(): List<Album> {
        return albumRepository.getAllAlbums()
    }

    suspend fun getTrackById(id: String): TrackDTO? {
        val trackUUID = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid UUID format for track ID")
        }
        val track = trackRepository.getTrackById(trackUUID) ?: return null
        return toTrackDTO(track)
    }

    suspend fun getAllTracks(): List<TrackDTO> {
        val tracks = trackRepository.getAllTracks()
        return tracks.map { toTrackDTO(it) }
    }

    suspend fun searchTracks(query: String): List<TrackDTO> {
        val tracks = trackRepository.searchTracksByName(query)
        return tracks.map { toTrackDTO(it) }
    }

    // MÉTODOS POST
    suspend fun createArtist(
        name: String,
        genre: String,
        imageBytes: ByteArray,
        imageFileName: String,
        contentType: String?
    ): Artist {
        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo de imagen no debe estar vacío.")
        }

        val fileKey = generateFileKey(imageFileName, "artists")
        val imageUrl = s3Service.uploadFile(imageBytes, fileKey, contentType)

        return artistRepository.createArtist(name = name, genre = genre, imageUrl = imageUrl)
    }

    suspend fun createAlbum(
        name: String,
        artistId: String,
        year: Int,
        albumArtBytes: ByteArray,
        albumArtFileName: String,
        contentType: String?
    ): Album {
        val artistUUID = UUID.fromString(artistId)

        artistRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")

        if (albumArtBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo de arte de álbum no debe estar vacío.")
        }

        val fileKey = generateFileKey(albumArtFileName, "albums")
        val albumArtUrl = s3Service.uploadFile(albumArtBytes, fileKey, contentType)

        return albumRepository.createAlbum(
            name = name,
            artistId = artistUUID,
            year = year,
            albumArtUrl = albumArtUrl
        )
    }

    suspend fun createTrack(
        name: String,
        albumId: String,
        duration: Long,
        artistId: String,
        previewBytes: ByteArray,
        previewFileName: String,
        contentType: String?
    ): Track {
        val artistUUID = UUID.fromString(artistId)
        val albumUUID = UUID.fromString(albumId)

        artistRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")
        albumRepository.getAlbumById(albumUUID)
            ?: throw IllegalArgumentException("Album ID not found")

        if (previewBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo de vista previa no debe estar vacío.")
        }

        val fileKey = generateFileKey(previewFileName, "tracks")
        val previewUrl = s3Service.uploadFile(previewBytes, fileKey, contentType)

        return trackRepository.createTrack(
            name = name,
            albumId = albumUUID,
            artistId = artistUUID,
            duration = duration,
            previewUrl = previewUrl
        )
    }
}