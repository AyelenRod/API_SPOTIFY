package Routes

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

    // Endpoint de Búsqueda
    get("/search") {
        val query = call.request.queryParameters["q"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")
        val trackDTOs = contentService.searchTracks(query)

        call.respond(SearchResponse(tracks = TracksWrapper(items = trackDTOs)))
    }

    // Endpoints de Creación (Protegido por JWT)
    authenticate("auth-jwt") {
        route("/artists") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")

                val multipart = call.receiveMultipart()
                var name: String? = null
                var genre: String? = null
                var imagePart: PartData.FileItem? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "name" -> name = part.value
                                "genre" -> genre = part.value
                            }
                        }
                        is PartData.FileItem -> if (part.name == "image") imagePart = part
                        else -> {}
                    }
                    part.dispose()
                }

                if (name == null || genre == null || imagePart == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Missing name, genre, or image file")
                }

                try {
                    val newArtist = contentService.createArtist(name!!, genre!!, imagePart!!)
                    call.respond(HttpStatusCode.Created, newArtist)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating artist: ${e.message}")
                }
            }
        }

        // POST /albums
        route("/albums") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")

                call.respond(HttpStatusCode.NotImplemented, "Album creation logic needs full implementation similar to Artist")
            }
        }

        // POST /tracks
        route("/tracks") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") return@post call.respond(HttpStatusCode.Forbidden, "Requires ADMIN role")

                call.respond(HttpStatusCode.NotImplemented, "Track creation logic needs full implementation similar to Artist")
            }
        }
    }
}
