package com.musicapp.services

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.models.TrackDTO
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.TrackRepository
import java.util.*

// El constructor debe recibir las instancias de los repositorios
class ContentService(
    private val s3Service: S3Service,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository
) {

    private fun generateFileKey(originalFileName: String, folder: String): String {
        val extension = originalFileName.substringAfterLast('.', "")
        val uniqueId = UUID.randomUUID().toString()
        return "$folder/$uniqueId.$extension"
    }

    // Convertidor de Track a TrackDTO (CORREGIDO)
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
            // CORRECCIÓN: Usa la propiedad correcta 'albumArt'
            albumArt = album.albumArt,
            duration = track.duration,
            previewUrl = track.previewUrl
        )
    }

    // Busca canciones por query
    suspend fun searchTracks(query: String): List<TrackDTO> {
        // CORRECCIÓN: Usar la instancia inyectada: trackRepository.searchTracksByName
        val tracks = trackRepository.searchTracksByName(query)
        // La función map crea un contexto de coroutine, resolviendo el error de suspensión
        return tracks.map { toTrackDTO(it) }
    }

    // Crea un nuevo artista
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

        // CORRECCIÓN: Usar la instancia inyectada: artistRepository.createArtist
        return artistRepository.createArtist(name = name, genre = genre, imageUrl = imageUrl)
    }

    // Crea un nuevo álbum
    suspend fun createAlbum(
        name: String,
        artistId: String,
        year: Int,
        albumArtBytes: ByteArray,
        albumArtFileName: String,
        contentType: String?
    ): Album {
        val artistUUID = UUID.fromString(artistId)

        // CORRECCIÓN: Usar la instancia inyectada: artistRepository.getArtistById
        artistRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")

        if (albumArtBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo de arte de álbum no debe estar vacío.")
        }

        val fileKey = generateFileKey(albumArtFileName, "albums")
        val albumArtUrl = s3Service.uploadFile(albumArtBytes, fileKey, contentType)

        // CORRECCIÓN: Usar la instancia inyectada: albumRepository.createAlbum
        return albumRepository.createAlbum(
            name = name,
            artistId = artistUUID,
            year = year,
            albumArtUrl = albumArtUrl
        )
    }

    // Crea una nueva canción/pista
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

        // CORRECCIÓN: Usar la instancia inyectada: artistRepository.getArtistById
        artistRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")
        // CORRECCIÓN: Usar la instancia inyectada: albumRepository.getAlbumById
        albumRepository.getAlbumById(albumUUID)
            ?: throw IllegalArgumentException("Album ID not found")

        if (previewBytes.isEmpty()) {
            throw IllegalArgumentException("El archivo de vista previa no debe estar vacío.")
        }

        val fileKey = generateFileKey(previewFileName, "tracks")
        val previewUrl = s3Service.uploadFile(previewBytes, fileKey, contentType)

        // CORRECCIÓN: Usar la instancia inyectada: trackRepository.createTrack
        return trackRepository.createTrack(
            name = name,
            albumId = albumUUID,
            artistId = artistUUID,
            duration = duration,
            previewUrl = previewUrl
        )
    }
}