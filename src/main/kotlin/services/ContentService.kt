package com.musicapp.services

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import com.musicapp.models.TrackDTO
import com.musicapp.repos.AlbumRepository
import com.musicapp.repos.ArtistRepository
import com.musicapp.repos.TrackRepository
import io.ktor.http.content.*
import java.util.*

class ContentService(private val s3Service: S3Service) {
    // Convierte una entidad Track a TrackDTO incluyendo información del artista y álbum
    private suspend fun toTrackDTO(track: Track): TrackDTO {
        val artist = ArtistRepository.findById(track.artistId)
            ?: throw IllegalStateException("Artist not found for track ${track.id}")

        val album = AlbumRepository.findById(track.albumId)
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

    // Busca canciones por query (nombre de canción, artista o álbum)
    suspend fun searchTracks(query: String): List<TrackDTO> {
        val tracks = TrackRepository.search(query)
        return tracks.map { toTrackDTO(it) }
    }

    // Crea un nuevo artista y sube su imagen a S3
    suspend fun createArtist(name: String, genre: String, imagePart: PartData.FileItem): Artist {
        val fileKey = "artists/${UUID.randomUUID()}-${imagePart.originalFileName}"
        val imageUrl = s3Service.uploadFile(imagePart, fileKey)

        val newArtist = Artist(name = name, genre = genre, image = imageUrl)

        return ArtistRepository.create(newArtist)
    }

    // Crea un nuevo álbum y sube su portada a S3
    suspend fun createAlbum(name: String, artistId: String, year: Int, albumArtPart: PartData.FileItem): Album {
        val artistUUID = UUID.fromString(artistId)

        ArtistRepository.findById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")

        val fileKey = "albums/${UUID.randomUUID()}-${albumArtPart.originalFileName}"
        val albumArtUrl = s3Service.uploadFile(albumArtPart, fileKey)

        val newAlbum = Album(
            name = name,
            artistId = artistUUID,
            year = year,
            albumArt = albumArtUrl
        )

        return AlbumRepository.create(newAlbum)
    }

    // Crea una nueva canción y sube su preview a S3
    suspend fun createTrack(
        name: String,
        albumId: String,
        duration: Long,
        artistId: String,
        previewPart: PartData.FileItem
    ): Track {
        val artistUUID = UUID.fromString(artistId)
        val albumUUID = UUID.fromString(albumId)

        ArtistRepository.findById(artistUUID)
            ?: throw IllegalArgumentException("Artist ID not found")
        AlbumRepository.findById(albumUUID)
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

        return TrackRepository.create(newTrack)
    }
}