import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
}

group = "sirgl"
version = "1.0-SNAPSHOT"

repositories {
    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    implementation("com.github.Kotlin:kotlinx.cli:-SNAPSHOT")
    testCompile("org.jetbrains.kotlin", "kotlin-test-junit5", "1.2.61")
    testCompile("org.junit.jupiter", "junit-jupiter-params", "5.1.0")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}