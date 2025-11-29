package com.musicapp.repos

import com.musicapp.models.Album
import com.musicapp.models.Artist
import com.musicapp.models.Track
import java.util.*

object MemoryRepository {
    private val artists = mutableListOf<Artist>()
    private val albums = mutableListOf<Album>()
    private val tracks = mutableListOf<Track>()

    // Inicializa con datos de ejemplo
    init {
        val artist1 = Artist(
            UUID.randomUUID(),
            "The Beatles",
            "Rock",
            "https://s3-url/beatles.jpg"
        )
        val artist2 = Artist(
            UUID.randomUUID(),
            "Daft Punk",
            "Electronic",
            "https://s3-url/daftpunk.jpg"
        )
        artists.add(artist1)
        artists.add(artist2)

        val album1 = Album(
            UUID.randomUUID(),
            "Abbey Road",
            artist1.id,
            "https://s3-url/abbeyroad.jpg",
            1969
        )
        val album2 = Album(
            UUID.randomUUID(),
            "Discovery",
            artist2.id,
            "https://s3-url/discovery.jpg",
            2001
        )
        albums.add(album1)
        albums.add(album2)

        tracks.add(Track(
            UUID.randomUUID(),
            "Come Together",
            259000,
            "https://s3-url/come_together.mp3",
            album1.id,
            artist1.id
        ))
        tracks.add(Track(
            UUID.randomUUID(),
            "Here Comes the Sun",
            185000,
            "https://s3-url/here_comes_the_sun.mp3",
            album1.id,
            artist1.id
        ))
        tracks.add(Track(
            UUID.randomUUID(),
            "One More Time",
            320000,
            "https://s3-url/one_more_time.mp3",
            album2.id,
            artist2.id
        ))
    }

    //Métodos para ARTISTS

    fun getAllArtists(): List<Artist> = artists
    fun getArtistById(id: UUID): Artist? = artists.find { it.id == id }
    fun addArtist(artist: Artist) {
        artists.add(artist)
    }

    //Métodos para ALBUMS

    fun getAllAlbums(): List<Album> = albums
    fun getAlbumById(id: UUID): Album? = albums.find { it.id == id }
    fun addAlbum(album: Album) {
        albums.add(album)
    }

    //Métodos para TRACKS

    fun getAllTracks(): List<Track> = tracks
    fun getTrackById(id: UUID): Track? = tracks.find { it.id == id }
    fun addTrack(track: Track) {
        tracks.add(track)
    }

    //Métodos de búsqueda y relaciones

    //Obtiene el álbum de una canción específica

    fun getAlbumByTrackId(trackId: UUID): Album? {
        val track = getTrackById(trackId) ?: return null
        return getAlbumById(track.albumId)
    }

    //Obtiene el artista de una canción específica

    fun getArtistByTrackId(trackId: UUID): Artist? {
        val track = getTrackById(trackId) ?: return null
        return getArtistById(track.artistId)
    }

    //Busca canciones que coincidan con el query

    fun search(query: String): List<Track> {
        val q = query.lowercase()

        return tracks.filter { track ->
            val artist = getArtistById(track.artistId)?.name?.lowercase() ?: ""
            val album = getAlbumById(track.albumId)?.name?.lowercase() ?: ""

            track.name.lowercase().contains(q) || artist.contains(q) || album.contains(q)
        }
    }
}