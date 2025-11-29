<<<<<<< HEAD
package services
=======
package com.musicapp.services
>>>>>>> api2

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.models.TrackDTO
import com.musicapp.repos.MemoryRepository
<<<<<<< HEAD
import java.util.*

class ContentService(private val s3Service: S3Service) {
    // Funciones de Transformación (Flattening)
    private fun toTrackDTO(track: Track): TrackDTO {
        val artist = MemoryRepository.getArtistById(track.artistId) ?: throw IllegalStateException("Artist not found")
        val album = MemoryRepository.getAlbumById(track.albumId) ?: throw IllegalStateException("Album not found")

=======
import io.ktor.http.content.*
import java.util.*

class ContentService(private val s3Service: S3Service) {
    private fun toTrackDTO(track: Track): TrackDTO {

        val artist = MemoryRepository.getArtistById(track.artistId)
            ?: throw IllegalStateException("Artist not found for track ${track.id}")

        val album = MemoryRepository.getAlbumById(track.albumId)
            ?: throw IllegalStateException("Album not found for track ${track.id}")

        // Construye y retorna el DTO con toda la información
>>>>>>> api2
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

<<<<<<< HEAD
=======
 //Busca una cancion
>>>>>>> api2
    fun searchTracks(query: String): List<TrackDTO> {
        return MemoryRepository.search(query).map { toTrackDTO(it) }
    }

<<<<<<< HEAD

    // Crea un Artista con imagen subida a S3
    suspend fun createArtist(name: String, genre: String, imagePart: PartData.FileItem): Artist {
        val fileKey = "artists/${UUID.randomUUID()}-${imagePart.originalFileName}"
        val imageUrl = s3Service.uploadFile(imagePart, fileKey)
        val newArtist = Artist(name = name, genre = genre, image = imageUrl)
        MemoryRepository.addArtist(newArtist)
        return newArtist
    }

    // Crea un Álbum con carátula subida a S3
    suspend fun createAlbum(name: String, artistId: String, year: Int, albumArtPart: PartData.FileItem): Album {
        val artistUUID = UUID.fromString(artistId)
        MemoryRepository.getArtistById(artistUUID) ?: throw IllegalArgumentException("Artist ID not found")

        val fileKey = "albums/${UUID.randomUUID()}-${albumArtPart.originalFileName}"
        val albumArtUrl = s3Service.uploadFile(albumArtPart, fileKey)
        val newAlbum = Album(name = name, artistId = artistUUID, year = year, albumArt = albumArtUrl)
        MemoryRepository.addAlbum(newAlbum)
        return newAlbum
    }

    // Crea una Canción con audio subido a S3
    suspend fun createTrack(name: String, albumId: String, duration: Long, artistId: String, previewPart: PartData.FileItem): Track {
        val artistUUID = UUID.fromString(artistId)
        val albumUUID = UUID.fromString(albumId)
        MemoryRepository.getArtistById(artistUUID) ?: throw IllegalArgumentException("Artist ID not found")
        MemoryRepository.getAlbumById(albumUUID) ?: throw IllegalArgumentException("Album ID not found")

        val fileKey = "tracks/${UUID.randomUUID()}-${previewPart.originalFileName}"
        val previewUrl = s3Service.uploadFile(previewPart, fileKey)
        val newTrack = Track(name = name, albumId = albumUUID, artistId = artistUUID, duration = duration, previewUrl = previewUrl)
        MemoryRepository.addTrack(newTrack)
        return newTrack
    }
}
=======
    //Crea un nuevo artista y sube su imagen a S3
    suspend fun createArtist(name: String, genre: String, imagePart: PartData.FileItem): Artist {
        val fileKey = "artists/${UUID.randomUUID()}-${imagePart.originalFileName}"

        val imageUrl = s3Service.uploadFile(imagePart, fileKey)

        val newArtist = Artist(name = name, genre = genre, image = imageUrl)
        MemoryRepository.addArtist(newArtist)

        return newArtist
    }

    //Crea un nuevo album y sube su caratula a S3
    suspend fun createAlbum(name: String, artistId: String, year: Int, albumArtPart: PartData.FileItem): Album {
        val artistUUID = UUID.fromString(artistId)

        MemoryRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")

        val fileKey = "albums/${UUID.randomUUID()}-${albumArtPart.originalFileName}"

        val albumArtUrl = s3Service.uploadFile(albumArtPart, fileKey)

        val newAlbum = Album(
            name = name,
            artistId = artistUUID,
            year = year,
            albumArt = albumArtUrl
        )

        MemoryRepository.addAlbum(newAlbum)

        return newAlbum
    }

    //Crea una nueva canción y sube su archivo de audio a S3
    suspend fun createTrack(
        name: String,
        albumId: String,
        duration: Long,
        artistId: String,
        previewPart: PartData.FileItem
    ): Track {
        val artistUUID = UUID.fromString(artistId)

        val albumUUID = UUID.fromString(albumId)

        MemoryRepository.getArtistById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")

        MemoryRepository.getAlbumById(albumUUID)
            ?: throw IllegalArgumentException("Album ID not found")

        val fileKey = "tracks/${UUID.randomUUID()}-${previewPart.originalFileName}"

        val previewUrl = s3Service.uploadFile(previewPart, fileKey)

        val newTrack = Track(
            name = name,
            albumId = albumUUID,
            artistId = artistUUID,
            duration = duration,
            previewUrl = previewUrl
        )

        MemoryRepository.addTrack(newTrack)

        return newTrack
    }
}
>>>>>>> api2
