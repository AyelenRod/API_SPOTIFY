package com.musicapp.routes

import com.musicapp.services.ContentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.ContentRouting(service: ContentService) {

    // ARTISTAS

    // POST /artistas
    post("/artistas") {
        try {
            val request = call.receive<Map<String, String>>()

            val name = request["name"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'name' field")
            val genre = request["genre"]
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'genre' field")

            val newArtist = service.createArtist(name, genre)

            call.respond(HttpStatusCode.Created, newArtist)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error creating artist: ${e.message}"
            ))
        }
    }

    // GET /artistas
    get("/artistas") {
        try {
            val artists = service.getAllArtists()
            call.respond(HttpStatusCode.OK, artists)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving artists: ${e.message}"
            ))
        }
    }

    // GET /artistas/{id}
    get("/artistas/{id}") {
        val artistId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

        try {
            val artist = service.getArtistById(artistId)
            if (artist == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artist not found"))
            } else {
                call.respond(HttpStatusCode.OK, artist)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (e.message ?: "Invalid ID format")
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving artist: ${e.message}"
            ))
        }
    }

    // PUT /artistas/{id}
    put("/artistas/{id}") {
        val artistId = call.parameters["id"]
            ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

        try {
            val request = call.receive<Map<String, String>>()
            val name = request["name"]
            val genre = request["genre"]

            val updatedArtist = service.updateArtist(artistId, name, genre)

            if (updatedArtist == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artist not found"))
            } else {
                call.respond(HttpStatusCode.OK, updatedArtist)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error updating artist: ${e.message}"
            ))
        }
    }

    // DELETE /artistas/{id}
    delete("/artistas/{id}") {
        val artistId = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

        try {
            val deleted = service.deleteArtist(artistId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Artist deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Artist not found"))
            }
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.Conflict, mapOf(
                "error" to (e.message ?: "Cannot delete artist with related data")
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error deleting artist: ${e.message}"
            ))
        }
    }

    // ALBUMES

    // POST /albumes
    post("/albumes") {
        try {
            val request = call.receive<Map<String, Any>>()

            val title = request["title"]?.toString()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'title' field")
            val releaseYear = (request["releaseYear"] as? Number)?.toInt()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'releaseYear' field")
            val artistId = request["artistId"]?.toString()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'artistId' field")
            val newAlbum = service.createAlbum(title, artistId, releaseYear)

            call.respond(HttpStatusCode.Created, newAlbum)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error creating album: ${e.message}"
            ))
        }
    }

    // GET /albumes
    get("/albumes") {
        try {
            val albums = service.getAllAlbums()
            call.respond(HttpStatusCode.OK, albums)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving albums: ${e.message}"
            ))
        }
    }

    // GET /albumes/{id}
    get("/albumes/{id}") {
        val albumId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing album ID")

        try {
            val album = service.getAlbumById(albumId)
            if (album == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Album not found"))
            } else {
                call.respond(HttpStatusCode.OK, album)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (e.message ?: "Invalid ID format")
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving album: ${e.message}"
            ))
        }
    }

    // PUT /albumes/{id}
    put("/albumes/{id}") {
        val albumId = call.parameters["id"]
            ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing album ID")

        try {
            val request = call.receive<Map<String, Any>>()
            val title = request["title"]?.toString()
            val releaseYear = (request["releaseYear"] as? Number)?.toInt()

            val updatedAlbum = service.updateAlbum(albumId, title, releaseYear)

            if (updatedAlbum == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Album not found"))
            } else {
                call.respond(HttpStatusCode.OK, updatedAlbum)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error updating album: ${e.message}"
            ))
        }
    }

    // DELETE /albumes/{id}
    delete("/albumes/{id}") {
        val albumId = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing album ID")

        try {
            val deleted = service.deleteAlbum(albumId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Album deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Album not found"))
            }
        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.Conflict, mapOf(
                "error" to (e.message ?: "Cannot delete album with related tracks")
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error deleting album: ${e.message}"
            ))
        }
    }

    //TRACKS

    // POST /tracks
    post("/tracks") {
        try {
            val request = call.receive<Map<String, Any>>()

            val title = request["title"]?.toString()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'title' field")
            val duration = (request["duration"] as? Number)?.toLong()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'duration' field")
            val albumId = request["albumId"]?.toString()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing 'albumId' field")
            val newTrack = service.createTrack(title, albumId, duration)

            call.respond(HttpStatusCode.Created, newTrack)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error creating track: ${e.message}"
            ))
        }
    }

    // GET /tracks
    get("/tracks") {
        try {
            val tracks = service.getAllTracks()
            call.respond(HttpStatusCode.OK, tracks)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving tracks: ${e.message}"
            ))
        }
    }

    // GET /tracks/{id}
    get("/tracks/{id}") {
        val trackId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing track ID")

        try {
            val track = service.getTrackById(trackId)
            if (track == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Track not found"))
            } else {
                call.respond(HttpStatusCode.OK, track)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to (e.message ?: "Invalid ID format")
            ))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error retrieving track: ${e.message}"
            ))
        }
    }

    // PUT /tracks/{id}
    put("/tracks/{id}") {
        val trackId = call.parameters["id"]
            ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing track ID")

        try {
            val request = call.receive<Map<String, Any>>()
            val title = request["title"]?.toString()
            val duration = (request["duration"] as? Number)?.toLong()

            val updatedTrack = service.updateTrack(trackId, title, duration)

            if (updatedTrack == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Track not found"))
            } else {
                call.respond(HttpStatusCode.OK, updatedTrack)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error updating track: ${e.message}"
            ))
        }
    }

    // DELETE /tracks/{id}
    delete("/tracks/{id}") {
        val trackId = call.parameters["id"]
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing track ID")

        try {
            val deleted = service.deleteTrack(trackId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Track deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Track not found"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Error deleting track: ${e.message}"
            ))
        }
    }
}