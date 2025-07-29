group = "swot"
version = "0.1"

plugins {
    kotlin("jvm") version "2.0.20" apply true

    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    testImplementation("junit", "junit", "4.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

application {
    mainClass.set("swot.CompilerKt")
}

tasks.register<JavaExec>("runDemo") {
    group = "application"
    description = "Run the n8n customer service demo"
    mainClass.set("swot.customerservice.example.DemoKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.withType<Test> {
    useJUnit()

    testLogging {
        events("passed", "skipped", "failed")
    }
}
