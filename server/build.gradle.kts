plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass = "com.knotkt.cs26.server.ApplicationKt"
}

dependencies {
    implementation(project(":contracts"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.postgresql)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit5)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())
}

tasks.test {
    useJUnitPlatform()
}
