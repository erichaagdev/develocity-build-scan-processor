plugins {
    id("java-library")
    id("org.openapi.generator") version "7.0.1"
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
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
}

openApiGenerate {
    generatorName = "java"
    inputSpec = layout.projectDirectory.file(provider { "gradle-enterprise-2023.3-api.yaml"} ).map { it.asFile.absolutePath }
    outputDir = layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath }
    modelPackage = "$group.model"
    apiPackage = "$group.api"
    invokerPackage = "$group.invoker"
    cleanupOutput = true
    openapiNormalizer = mapOf("REF_AS_PARENT_IN_ALLOF" to "true")
    // see https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md for a description of each configuration option
    configOptions = mapOf(
        "additionalModelTypeAnnotations" to "@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
        "containerDefaultToNull" to "true",
        "disallowAdditionalPropertiesIfNotPresent" to "false",
        "hideGenerationTimestamp" to "true",
        "library" to "native",
        "openApiNullable" to "false",
        "useBeanValidation" to "false",
        "useJakartaEe" to "true",
    )
}

val generateApiModels by tasks.registering(Sync::class) {
    from(tasks.openApiGenerate) {
        includeEmptyDirs = false
        include("src/main/java/dev/erichaag/develocity/model/*")
        eachFile {
            path = path.removePrefix("src/main/java")
        }
    }
    into(layout.buildDirectory.dir("generated/apiModels"))
}

sourceSets {
    main {
        java {
            srcDir(generateApiModels)
        }
    }
}
