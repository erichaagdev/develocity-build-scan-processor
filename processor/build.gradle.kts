plugins {
    id("dev.erichaag.java-library")
}

dependencies {
    api(project(":api"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
}
