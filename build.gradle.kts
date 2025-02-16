import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // Kotlin plugin
    kotlin("jvm") version "1.9.10"
    // Alkalmazás plugin
    application
    // Jlink plugin (natív csomag, jpackage)
    id("org.beryx.jlink") version "2.26.0" // vagy a legfrissebb, ha más elérhető

    // Shadow plugin (fat JAR)
    id("com.github.johnrengelman.shadow") version "8.1.1" // vagy a legfrissebb




}

group = "demo.fvprojekt"
version = "1.0"

repositories {
    // Függőségek letöltése
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // Példa extra libek:
    implementation("net.objecthunter:exp4j:0.4.8")

    implementation("com.formdev:flatlaf:3.1")
}

application {
    // Ha nincs csomagnév, a Main.kt a src/main/kotlin-ben van "top-level" main() függvénnyel
    // => a mainClass "MainKt" lesz
    mainClass.set("MainKt")
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




// Shadow plugin testreszabás - fat JAR
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
}

// Jlink plugin - jpackage beállítás
jlink {
    // Minimális Java runtime kép
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        // A generált futtatható bináris neve
        name = "FuvSzam"
    }
    // Az image mappa
    imageDir.set(layout.buildDirectory.dir("image"))
    // Ha natív telepítőt szeretnél (deb, exe, msi, dmg, pkg, stb.)
    jpackage {
        // Példa: Linux deb
        installerType = "deb"
        installerOptions.addAll(listOf("--linux-shortcut"))
    }
}
