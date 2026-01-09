import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("org.beryx.runtime") version "2.0.1"
}

group = "info.skyblond"
version = System.getenv("GITHUB_REF_NAME")?.removePrefix("v") ?: "dev"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.3")
    implementation("com.github.ajalt.mordant:mordant:3.0.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    implementation("org.tukaani:xz:1.11")

    testImplementation("org.jsoup:jsoup:1.17.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

application {
    mainClass.set("info.skyblond.ctc.MainKt")
    executableDir = ""
    applicationName = "ctc"
}

runtime {
    options = listOf("--compress", "zip-9", "--no-header-files", "--no-man-pages")
    addModules("java.base", "java.logging", "java.management")
    val jdkVersion = "zulu21.32.17-ca-jdk21.0.2"
    targetPlatform("linux_x64") {
        jdkHome = jdkDownload("https://cdn.azul.com/zulu/bin/${jdkVersion}-linux_x64.zip")
    }
    targetPlatform("linux_aarch64") {
        jdkHome = jdkDownload("https://cdn.azul.com/zulu/bin/${jdkVersion}-linux_aarch64.tar.gz")
    }
    targetPlatform("win_x64") {
        jdkHome = jdkDownload("https://cdn.azul.com/zulu/bin/${jdkVersion}-win_x64.zip")
    }
    targetPlatform("macosx_x64") {
        jdkHome = jdkDownload("https://cdn.azul.com/zulu/bin/${jdkVersion}-macosx_x64.zip")
    }
    targetPlatform("macosx_aarch64") {
        jdkHome = jdkDownload("https://cdn.azul.com/zulu/bin/${jdkVersion}-macosx_aarch64.zip")
    }
}

