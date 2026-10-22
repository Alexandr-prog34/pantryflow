
plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.4.0"
    alias(ktorLibs.plugins.ktor)
}

group = "com.pantryflow"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.callLogging)
    implementation("io.ktor:ktor-server-swagger:3.6.0")
    implementation("org.webjars:swagger-ui:5.20.1")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.hibernate.orm:hibernate-core:6.6.42.Final")
    implementation("org.flywaydb:flyway-core:11.8.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.8.2")
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
