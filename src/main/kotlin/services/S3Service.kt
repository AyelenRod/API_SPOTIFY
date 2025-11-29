package com.musicapp.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.http.content.*
import io.ktor.server.config.*

class S3Service(config: ApplicationConfig) {
    private val bucketName: String = config.property("aws.s3.bucketName").getString()
    private val region: String = config.property("aws.s3.region").getString()
    private val s3Client: S3Client = S3Client {
        this.region = this@S3Service.region
    }

    // URL base del bucket para construir URLs públicas de los archivos
    private val bucketBaseUrl = "https://$bucketName.s3.$region.amazonaws.com/"

    // Sube un archivo a S3 y devuelve su URL pública
    suspend fun uploadFile(part: PartData.FileItem, fileKey: String): String {
        val fileBytes = part.streamProvider().readBytes()

        // Crea la petición de subida
        val request = PutObjectRequest {
            bucket = bucketName
            key = fileKey
            contentType = part.contentType?.contentType
            body = ByteStream.fromBytes(fileBytes)
        }

        // Ejecuta la subida a S3 (operación suspendida, no bloquea el thread)
        s3Client.putObject(request)

        return "$bucketBaseUrl$fileKey"
    }
}