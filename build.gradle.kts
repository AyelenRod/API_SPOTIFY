plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.3"
    id("com.gradleup.shadow") version "8.3.5"
    application
}

group = "com.musicapp"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.musicapp.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("mi-api2.jar")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // KTOR CORE & SERVER
    implementation("io.ktor:ktor-server-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.3")

    // SERIALIZACIÓN JSON (Jackson)
    implementation("io.ktor:ktor-serialization-jackson-jvm:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.3")

    // CORS
    implementation("io.ktor:ktor-server-cors-jvm:3.0.3")

    // BASE DE DATOS - PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.4")

    // HikariCP - Pool de conexiones
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Exposed ORM (Manejo de SQL en Kotlin)
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.57.0")

    // LOGGING (Necesario para ver logs de Ktor y SQL)
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.slf4j:slf4j-api:2.0.16")

    // TESTING
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}