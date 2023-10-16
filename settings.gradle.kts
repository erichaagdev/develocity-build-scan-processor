plugins {
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.11.3"
    id("com.gradle.enterprise") version "3.15.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceAgree = "yes"
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
    }
}

include(":samples")

rootProject.name = "develocity-build-processor"
