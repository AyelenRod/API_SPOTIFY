package com.musicapp.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.http.content.*
import io.ktor.server.config.*

class S3Service(config: ApplicationConfig) {
    private val bucketName: String = config.property("s3.bucketName").getString()

    private val region: String = config.property("aws.region").getString()

    // Crea el cliente de S3 con la región configurada
    private val s3Client: S3Client = S3Client {
        // El SDK de AWS buscará las credenciales automáticamente en ~/.aws/credentials
        this.region = this@S3Service.region
    }

    // URL base del bucket para construir URLs públicas
    private val bucketBaseUrl = "https://$bucketName.s3.$region.amazonaws.com/"

    suspend fun uploadFile(part: PartData.FileItem, fileKey: String): String {
        val fileBytes = part.streamProvider().readBytes()

        // Construye la solicitud de subida
        val request = PutObjectRequest {
            bucket = bucketName
            key = fileKey
            contentType = part.contentType?.contentType
            body = ByteStream.fromBytes(fileBytes)
        }

        s3Client.putObject(request)

        // Construye y retorna la URL pública
        return "$bucketBaseUrl$fileKey"
    }
}