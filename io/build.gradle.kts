plugins {
    kotlin("jvm")
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
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}