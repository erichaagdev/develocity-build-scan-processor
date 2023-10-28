plugins {
    id("java-library")
}

group = "dev.erichaag.develocity"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":api"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
}
