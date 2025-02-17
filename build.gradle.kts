import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {

    kotlin("jvm") version "1.9.10"


    application


    id("org.beryx.jlink") version "2.26.0"


    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "demo.fvprojekt"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))


    implementation("net.objecthunter:exp4j:0.4.8")


    implementation("com.formdev:flatlaf:3.1")
}

application {

    mainClass.set("demo.fvprojekt.demo.fvprojekt.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")

    manifest {
        attributes["Main-Class"] = "demo.fvprojekt.demo.fvprojekt.MainKt"
    }
}


jlink {

    mergedModuleName = "fuvgeny.merged"

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = "FuvSzam"
    }


    imageDir.set(layout.buildDirectory.dir("image"))


    jpackage {
        installerType = "exe"

        //installerOptions.addAll(listOf("--linux-shortcut"))
    }
}
