@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.erichaag.java-library")
    id("org.openapi.generator")
}

val develocityApiExtension = extensions.create<DevelocityApiExtension>("develocityApi")

repositories {
    exclusiveContent {
        forRepository {
            ivy {
                name = "Develocity API Specification"
                url = uri("https://docs.gradle.com/enterprise/api-manual/ref")
                patternLayout { artifact("/gradle-enterprise-[revision]-api.yaml") }
                metadataSources { artifact() }
            }
        }
        filter { includeModule("com.gradle", "develocity-api-specification") }
    }
}

val develocityApiSpecification: DependencyScopeConfiguration = configurations.dependencyScope("develocityApiSpecification") {
    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, "develocity-api-specification"))
}.get()

val resolvableDevelocityApiSpecification = configurations.resolvable("resolvableDevelocityApiSpecification") {
    extendsFrom(develocityApiSpecification)
    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, "develocity-api-specification"))
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    develocityApiSpecification(develocityApiExtension.version.map { "com.gradle:develocity-api-specification:$it" })
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
}

openApiGenerate {
    generatorName = "java"
    inputSpec = resolvableDevelocityApiSpecification.map { it.singleFile.absolutePath }
    outputDir = layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath }
    modelPackage = "$group.api"
    apiPackage = "$group.unused.api"
    invokerPackage = "$group.unused.invoker"
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
        include("src/main/java/dev/erichaag/develocity/api/*")
        eachFile {
            path = path.removePrefix("src/main/java")
        }
    }
    into(layout.buildDirectory.dir("generated/apiModels"))
}

val generate by tasks.registering {
    dependsOn(generateApiModels)
}

sourceSets {
    main {
        java {
            srcDir(generateApiModels)
        }
    }
}

abstract class DevelocityApiExtension {
    abstract val version: Property<String>
}
