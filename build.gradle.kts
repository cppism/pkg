plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

application {
    mainClass = "pkg.MainKt"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.io)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kaml)
    runtimeOnly(libs.logback.classic)
}