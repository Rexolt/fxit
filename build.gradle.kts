plugins {
    kotlin("jvm") version "2.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.3.0")
    testImplementation(kotlin("test"))
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.formdev:flatlaf:3.0")
    implementation("com.formdev:flatlaf:3.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}