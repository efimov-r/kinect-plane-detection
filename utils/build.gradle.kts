plugins {
    // Apply the shared build logic from a convention plugin. The shared code is located in buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from "gradle/libs.versions.toml"
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // Apply KotlinX bundle of dependencies from the version catalog ("gradle/libs.versions.toml")
    implementation(libs.bundles.kotlinxEcosystem)

    implementation(files("lib/jocl-2.0.4.jar"))
    implementation(files("lib/jocl-blast-1.5.0.jar"))
    implementation(files("lib/protobuf-java-4.28.1.jar"))

    testImplementation(kotlin("test"))
}