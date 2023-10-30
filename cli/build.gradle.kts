plugins {
    id("dev.erichaag.java")
    id("application")
}

application {
    applicationName = "cli"
    mainClass = "dev.erichaag.develocity.cli.Main"
}

distributions {
    main {
        contents {
            from(layout.projectDirectory.file("config.properties"))
        }
    }
}

dependencies {
    implementation("com.jakewharton.picnic:picnic:0.7.0")
    implementation("info.picocli:picocli:4.7.5")
    implementation(project(":processor"))
}
