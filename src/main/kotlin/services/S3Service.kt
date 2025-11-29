package com.musicapp.services

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
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

    suspend fun uploadFile(fileBytes: ByteArray, fileKey: String, contentType: String?): String {

        // La verificación de vacío sigue siendo válida.
        if (fileBytes.isEmpty()) {
            throw IllegalStateException("El archivo está vacío")
        }

        println("Subiendo archivo: $fileKey (${fileBytes.size} bytes)")

        val request = PutObjectRequest {
            bucket = bucketName
            key = fileKey
            this.contentType = contentType ?: "application/octet-stream"
            body = ByteStream.fromBytes(fileBytes)
        }

        s3Client.putObject(request)

        println("Archivo subido exitosamente: $bucketBaseUrl$fileKey")

        return "$bucketBaseUrl$fileKey"
    }
}