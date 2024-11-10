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

    implementation(files("lib/jna.jar"))
    implementation(files("lib/opencv-4100.jar"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}