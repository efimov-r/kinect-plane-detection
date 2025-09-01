plugins {
    // Apply the shared build logic from a convention plugin. The shared code is located in buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application
    application
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--add-modules", "jdk.incubator.vector"))
}
tasks.withType<JavaExec> {
    jvmArgs("--add-modules", "jdk.incubator.vector")
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":utils"))
    implementation(project(":view"))
    implementation(project(":io"))
    implementation(project(":data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation(files("lib/jocl-2.0.4.jar"))
    implementation(files("lib/jocl-blast-1.5.0.jar"))
    implementation(files("lib/protobuf-java-4.28.1.jar"))
}

application {
    // Define the Fully Qualified Name for the application main class.
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "era.app.AppKt"
}
