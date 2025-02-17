import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // Kotlin plugin a JVM-hez
    kotlin("jvm") version "1.9.10"

    // Alkalmazás plugin – futtatható jar létrehozásához
    application

    // Beryx JLink plugin – ha natív telepítőt vagy minimalizált Java runtime-ot akarsz
    id("org.beryx.jlink") version "2.26.0"

    // Shadow plugin – „fat JAR” létrehozásához
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "demo.fvprojekt"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Példa: exp4j a függvények kiértékeléséhez
    implementation("net.objecthunter:exp4j:0.4.8")

    // Példa: FlatLaf a GUI témákhoz
    implementation("com.formdev:flatlaf:3.1")
}

application {
    // A 'package demo.fvprojekt.demo.fvprojekt' miatt:
    // A top-level main függvény a "demo.fvprojekt.demo.fvprojekt.MainKt" osztályba fordul
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

// Shadow plugin – „fat JAR” beállítás
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    // MANIFEST-beállítás, hogy a futtatható jar-ban is megtalálja a main függvényt
    manifest {
        attributes["Main-Class"] = "demo.fvprojekt.demo.fvprojekt.MainKt"
    }
}

// Beryx JLink plugin – ha minimalizált Java runtime-ot és natív telepítőt szeretnél
jlink {
    // Ha NINCS module-info.java, a plugin egy „merged” modult készít
    mergedModuleName = "fuvgeny.merged"

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = "FuvSzam"
    }

    // imageDir – hova tegye a minimalizált runtime image-et (opcionális)
    imageDir.set(layout.buildDirectory.dir("image"))

    // Ha natív telepítőt is akarsz, pl. Linux .deb:
    // Windows-on "exe", macOS-en "pkg" vagy "dmg" stb.
    jpackage {
        installerType = "exe"  // Linuxon .deb, Windows-on "exe"
        // Telepítő paraméterek (Linuxon pl. "--linux-shortcut")
        //installerOptions.addAll(listOf("--linux-shortcut"))
    }
}
