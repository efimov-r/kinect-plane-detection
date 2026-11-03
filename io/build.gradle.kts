plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "era"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":data"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation(files("lib/jna.jar"))
    implementation(files("lib/opencv-4100.jar"))

    implementation(files("lib/jocl-2.0.4.jar"))
    implementation(files("lib/jocl-blast-1.5.0.jar"))
    implementation(files("lib/protobuf-java-4.28.1.jar"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}