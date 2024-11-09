plugins {
    kotlin("jvm")
}

group = "era"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {

///////////////////////////////////////////////////////////////////////////////////////////////////////
////////opencl & opengl
///////////////////////////////////////////////////////////////////////////////////////////////////////
    implementation(files("lib/jocl-2.0.4.jar"))
    implementation(files("lib/jocl-blast-1.5.0.jar"))

    implementation(files("lib/protobuf-java-4.28.1.jar"))

//    implementation 'org.jocl:jocl:2.0.4'
//    implementation 'org.jocl:jocl-blast:1.5.0'
    implementation ("org.jogamp.gluegen:gluegen-rt-main:2.3.1")
    implementation ("org.jogamp.jogl:jogl-all-main:2.3.1")
    implementation ("org.lwjgl.lwjgl:lwjgl:2.9.3")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
///////////////////////////////////////////////////////////////////////////////////////////////////////


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}