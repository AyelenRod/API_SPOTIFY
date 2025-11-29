import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.9"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.musicapp"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.musicapp.ApplicationKt")
}

ktor {
    fatJar {
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //KTOR CORE & SERVER
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    //SERIALIZACIÓN JSON
    implementation("io.ktor:ktor-serialization-jackson-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    //SEGURIDAD Y AUTENTICACIÓN
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")

    //CORS
    implementation("io.ktor:ktor-server-cors-jvm")

    // STATUS PAGES
    implementation("io.ktor:ktor-server-status-pages-jvm")

    //AWS SDK
    implementation("aws.sdk.kotlin:s3:1.10.0")
    implementation("aws.sdk.kotlin:aws-core:1.10.0")
    implementation("aws.smithy.kotlin:http-client-engine-crt:1.0.0")

    //BASE DE DATOS - PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")

    // HikariCP - Pool de conexiones
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Exposed ORM - Framework de base de datos para Kotlin
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")

    // BCRYPT para hashear passwords
    implementation("org.mindrot:jbcrypt:0.4")

    //LOGGING
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.9")

    //TESTING
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<ShadowJar> {
    archiveFileName.set("mi-api2.jar")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    mergeServiceFiles()
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}