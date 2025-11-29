package models

data class TrackDTO(
    val id: String,
    val name: String,
    val artist: String,
    val album: String,
    val albumArt: String,
    val duration: Long,
    val previewUrl: String
)

data class SearchResponse(
    val tracks: TracksWrapper
)

data class TracksWrapper(
    val items: List<TrackDTO>
)
