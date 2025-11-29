package services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import io.ktor.http.content.*
import io.ktor.server.config.*
import java.io.File

class S3Service(config: ApplicationConfig) {
    private val bucketName: String = config.property("aws.s3.bucketName").getString()
    private val s3Client: S3Client = S3Client { region = config.property("aws.s3.region").getString() }
    private val bucketBaseUrl = "https://$bucketName.s3.${s3Client.config.region}.amazonaws.com/"

    // Sube un archivo a S3 y devuelve su URL pública
    suspend fun uploadFile(part: PartData.FileItem, fileKey: String): String {
        val fileBytes = part.streamProvider().readBytes()

        val request = PutObjectRequest {
            bucket = bucketName
            key = fileKey
            contentType = part.contentType?.contentType
            body = aws.smithy.kotlin.runtime.content.ByteStream.fromBytes(fileBytes)
        }

        s3Client.putObject(request)

        return "$bucketBaseUrl$fileKey"
    }
}
