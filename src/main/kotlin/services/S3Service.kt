package com.musicapp.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.ktor.http.content.*
import io.ktor.server.config.*
import io.ktor.utils.io.*

class S3Service(config: ApplicationConfig) {
    private val bucketName: String = config.propertyOrNull("s3.bucketName")?.getString()
        ?: throw IllegalStateException("ERROR: s3.bucketName no configurado en application.conf")

    private val region: String = config.propertyOrNull("aws.region")?.getString()
        ?: "us-east-1"

    private val s3Client: S3Client = S3Client {
        this.region = this@S3Service.region
    }

    private val bucketBaseUrl = "https://$bucketName.s3.$region.amazonaws.com/"

    suspend fun uploadFile(part: PartData.FileItem, fileKey: String): String {
        val fileBytes = part.streamProvider().readBytes()

        // Verificar que el archivo no esté vacío
        if (fileBytes.isEmpty()) {
            throw IllegalStateException("El archivo está vacío")
        }

        println("Subiendo archivo: $fileKey (${fileBytes.size} bytes)")

        val request = PutObjectRequest {
            bucket = bucketName
            key = fileKey
            contentType = part.contentType?.toString() ?: "application/octet-stream"
            body = ByteStream.fromBytes(fileBytes)
        }

        s3Client.putObject(request)

        println("Archivo subido exitosamente: $bucketBaseUrl$fileKey")

        return "$bucketBaseUrl$fileKey"
    }
}