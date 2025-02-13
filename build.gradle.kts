import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.beryx.jlink") version "2.25.0"
}


group = "demo.fvprojekt"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.formdev:flatlaf:3.1")
}

application {
    // Kotlin DSL-ben így állítod be a mainClass-t:
    mainClass.set("demo.fvprojekt.MainKt")
}

// Kotlin DSL-ben a kotlinOptions tipikusan így:
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "GraKot"
    }
    imageDir.set(file("$buildDir/image"))

    jpackage {
        installerType = "deb"
        installerOptions.addAll(listOf("--linux-shortcut"))
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}
