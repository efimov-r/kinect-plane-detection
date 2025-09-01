plugins {
    kotlin("jvm")
}

group = "era"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation(files("lib/jocl-2.0.4.jar"))
    implementation(files("lib/jocl-blast-1.5.0.jar"))
    implementation(files("lib/protobuf-java-4.28.1.jar"))
    testImplementation(kotlin("test"))
    implementation(project(":utils"))
}

tasks.test {
    useJUnitPlatform()
}