plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "com.vulnerable.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        // Gera informações de debug no bytecode
        freeCompilerArgs.add("-Xdebug")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}

application {
    mainClass.set("MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}