package com.musicapp.routes

import com.musicapp.models.SearchResponse
import com.musicapp.models.TracksWrapper
import com.musicapp.services.ContentService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.contentRouting(contentService: ContentService) {

    //HEALTH CHECK
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf(
            "status" to "UP",
            "service" to "MusicApp Backend",
            "timestamp" to System.currentTimeMillis()
        ))
    }

    // BÚSQUEDA
    get("/search") {
        val query = call.request.queryParameters["q"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")

        val trackDTOs = contentService.searchTracks(query)
        call.respond(SearchResponse(tracks = TracksWrapper(items = trackDTOs)))
    }

    // RUTAS PÚBLICAS - ARTISTAS

    // GET /artists - Listar todos los artistas
    get("/artists") {
        try {
            val artists = contentService.getAllArtists()
            call.respond(HttpStatusCode.OK, artists)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving artists: ${e.message}")
        }
    }

    // GET /artists/{id} - Obtener un artista por ID
    get("/artists/{id}") {
        val artistId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

        try {
            val artist = contentService.getArtistById(artistId)
            if (artist == null) {
                call.respond(HttpStatusCode.NotFound, "Artist not found")
            } else {
                call.respond(HttpStatusCode.OK, artist)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving artist: ${e.message}")
        }
    }

    // RUTAS PÚBLICAS - ÁLBUMES

    // GET /albums - Listar todos los álbumes
    get("/albums") {
        try {
            val albums = contentService.getAllAlbums()
            call.respond(HttpStatusCode.OK, albums)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving albums: ${e.message}")
        }
    }

    // GET /albums/{id} - Obtener un álbum por ID
    get("/albums/{id}") {
        val albumId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing album ID")

        try {
            val album = contentService.getAlbumById(albumId)
            if (album == null) {
                call.respond(HttpStatusCode.NotFound, "Album not found")
            } else {
                call.respond(HttpStatusCode.OK, album)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving album: ${e.message}")
        }
    }

    // RUTAS PÚBLICAS - TRACKS

    // GET /tracks - Listar todos los tracks
    get("/tracks") {
        try {
            val tracks = contentService.getAllTracks()
            call.respond(HttpStatusCode.OK, tracks)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving tracks: ${e.message}")
        }
    }

    // GET /tracks/{id} - Obtener un track por ID
    get("/tracks/{id}") {
        val trackId = call.parameters["id"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing track ID")

        try {
            val track = contentService.getTrackById(trackId)
            if (track == null) {
                call.respond(HttpStatusCode.NotFound, "Track not found")
            } else {
                call.respond(HttpStatusCode.OK, track)
            }
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid ID format")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error retrieving track: ${e.message}")
        }
    }


    // RUTAS PROTEGIDAS (ADMIN)
    authenticate("auth-jwt") {

        //  ARTISTAS

        // POST /artists - Crear artista
        route("/artists") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var genre: String? = null
                var imageBytes: ByteArray? = null
                var imageFileName: String? = null
                var imageContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "genre" -> genre = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                imageBytes = part.streamProvider().readBytes()
                                imageFileName = part.originalFileName
                                imageContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || genre == null || imageBytes == null || imageFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, genre, or image file")
                }

                try {
                    val newArtist = contentService.createArtist(
                        name!!,
                        genre!!,
                        imageBytes!!,
                        imageFileName!!,
                        imageContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newArtist)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating artist: ${e.message}")
                }
            }
        }

        // PUT /artists/{id} - Actualizar artista
        put("/artists/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@put call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val artistId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

            val multipart = call.receiveMultipart()
            var name: String? = null
            var genre: String? = null
            var imageBytes: ByteArray? = null
            var imageFileName: String? = null
            var imageContentType: ContentType? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "genre" -> genre = part.value
                        }
                        part.dispose()
                    }
                    is PartData.FileItem -> {
                        if (part.name == "image") {
                            imageBytes = part.streamProvider().readBytes()
                            imageFileName = part.originalFileName
                            imageContentType = part.contentType
                        }
                        part.dispose()
                    }
                    else -> part.dispose()
                }
            }

            try {
                val updatedArtist = contentService.updateArtist(
                    id = artistId,
                    name = name,
                    genre = genre,
                    imageBytes = imageBytes,
                    imageFileName = imageFileName,
                    contentType = imageContentType?.toString()
                )

                if (updatedArtist == null) {
                    call.respond(HttpStatusCode.NotFound, "Artist not found")
                } else {
                    call.respond(HttpStatusCode.OK, updatedArtist)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating artist: ${e.message}")
            }
        }

        // DELETE /artists/{id} - Eliminar artista
        delete("/artists/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@delete call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val artistId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing artist ID")

            try {
                val deleted = contentService.deleteArtist(artistId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Artist deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Artist not found")
                }
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Cannot delete artist with related data")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting artist: ${e.message}")
            }
        }

        //  ÁLBUMES

        // POST /albums - Crear álbum
        route("/albums") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var artistId: String? = null
                var year: String? = null
                var albumArtBytes: ByteArray? = null
                var albumArtFileName: String? = null
                var albumArtContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "artistId" -> artistId = part.value
                                "year" -> year = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "albumArt") {
                                albumArtBytes = part.streamProvider().readBytes()
                                albumArtFileName = part.originalFileName
                                albumArtContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || artistId == null || year == null || albumArtBytes == null || albumArtFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, artistId, year, or albumArt file")
                }

                try {
                    val newAlbum = contentService.createAlbum(
                        name!!,
                        artistId!!,
                        year!!.toInt(),
                        albumArtBytes!!,
                        albumArtFileName!!,
                        albumArtContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newAlbum)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating album: ${e.message}")
                }
            }
        }

        // PUT /albums/{id} - Actualizar álbum
        put("/albums/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@put call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val albumId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing album ID")

            val multipart = call.receiveMultipart()
            var name: String? = null
            var year: String? = null
            var albumArtBytes: ByteArray? = null
            var albumArtFileName: String? = null
            var albumArtContentType: ContentType? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "year" -> year = part.value
                        }
                        part.dispose()
                    }
                    is PartData.FileItem -> {
                        if (part.name == "albumArt") {
                            albumArtBytes = part.streamProvider().readBytes()
                            albumArtFileName = part.originalFileName
                            albumArtContentType = part.contentType
                        }
                        part.dispose()
                    }
                    else -> part.dispose()
                }
            }

            try {
                val updatedAlbum = contentService.updateAlbum(
                    id = albumId,
                    name = name,
                    year = year?.toIntOrNull(),
                    albumArtBytes = albumArtBytes,
                    albumArtFileName = albumArtFileName,
                    contentType = albumArtContentType?.toString()
                )

                if (updatedAlbum == null) {
                    call.respond(HttpStatusCode.NotFound, "Album not found")
                } else {
                    call.respond(HttpStatusCode.OK, updatedAlbum)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating album: ${e.message}")
            }
        }

        // DELETE /albums/{id} - Eliminar álbum
        delete("/albums/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@delete call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val albumId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing album ID")

            try {
                val deleted = contentService.deleteAlbum(albumId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Album deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Album not found")
                }
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "Cannot delete album with related tracks")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting album: ${e.message}")
            }
        }

        //  TRACKS

        // POST /tracks - Crear track
        route("/tracks") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()

                if (role != "ADMIN") {
                    return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
                }

                val multipart = call.receiveMultipart()
                var name: String? = null
                var albumId: String? = null
                var artistId: String? = null
                var duration: String? = null
                var previewBytes: ByteArray? = null
                var previewFileName: String? = null
                var previewContentType: ContentType? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "albumId" -> albumId = part.value
                                "artistId" -> artistId = part.value
                                "duration" -> duration = part.value
                            }
                            part.dispose()
                        }
                        is PartData.FileItem -> {
                            if (part.name == "preview") {
                                previewBytes = part.streamProvider().readBytes()
                                previewFileName = part.originalFileName
                                previewContentType = part.contentType
                            }
                            part.dispose()
                        }
                        else -> part.dispose()
                    }
                }

                if (name == null || albumId == null || artistId == null || duration == null || previewBytes == null || previewFileName == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, albumId, artistId, duration, or preview file")
                }

                try {
                    val newTrack = contentService.createTrack(
                        name!!,
                        albumId!!,
                        duration!!.toLong(),
                        artistId!!,
                        previewBytes!!,
                        previewFileName!!,
                        previewContentType?.toString()
                    )
                    call.respond(HttpStatusCode.Created, newTrack)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating track: ${e.message}")
                }
            }
        }

        // PUT /tracks/{id} - Actualizar track
        put("/tracks/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@put call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val trackId = call.parameters["id"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing track ID")

            val multipart = call.receiveMultipart()
            var name: String? = null
            var duration: String? = null
            var previewBytes: ByteArray? = null
            var previewFileName: String? = null
            var previewContentType: ContentType? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "name" -> name = part.value
                            "duration" -> duration = part.value
                        }
                        part.dispose()
                    }
                    is PartData.FileItem -> {
                        if (part.name == "preview") {
                            previewBytes = part.streamProvider().readBytes()
                            previewFileName = part.originalFileName
                            previewContentType = part.contentType
                        }
                        part.dispose()
                    }
                    else -> part.dispose()
                }
            }

            try {
                val updatedTrack = contentService.updateTrack(
                    id = trackId,
                    name = name,
                    duration = duration?.toLongOrNull(),
                    previewBytes = previewBytes,
                    previewFileName = previewFileName,
                    contentType = previewContentType?.toString()
                )

                if (updatedTrack == null) {
                    call.respond(HttpStatusCode.NotFound, "Track not found")
                } else {
                    call.respond(HttpStatusCode.OK, updatedTrack)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating track: ${e.message}")
            }
        }

        // DELETE /tracks/{id} - Eliminar track
        delete("/tracks/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()

            if (role != "ADMIN") {
                return@delete call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")
            }

            val trackId = call.parameters["id"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing track ID")

            try {
                val deleted = contentService.deleteTrack(trackId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Track deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Track not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error deleting track: ${e.message}")
            }
        }
    }
}