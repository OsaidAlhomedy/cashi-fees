plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    kotlin("plugin.allopen") version "2.3.10"
    id("org.springframework.boot") version "3.5.13"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.3.10"
}

group = "com.cashi"
version = "0.1.0"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

repositories { mavenCentral() }

// Restate-annotated classes must be open.
allOpen {
    annotation("dev.restate.sdk.annotation.Service")
    annotation("dev.restate.sdk.annotation.VirtualObject")
    annotation("dev.restate.sdk.annotation.Workflow")
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

extra["netty.version"] = "4.1.132.Final"
extra["kotlin-serialization.version"] = "1.11.0"

val restateVersion = "2.9.3"
val cucumberVersion = "7.20.1"

dependencies {
    implementation("dev.restate:sdk-spring-boot-kotlin-starter:$restateVersion")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.liquibase:liquibase-core")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("dev.restate:sdk-testing:$restateVersion")
    testRuntimeOnly("com.h2database:h2")
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
}

val cucumberReport = layout.buildDirectory.file("reports/cucumber.html")

val openCucumberReport = tasks.register("openCucumberReport") {
    description = "Open the test report"
    onlyIf { !providers.environmentVariable("CI").isPresent }
    doLast {
        val report = cucumberReport.get().asFile
        if (!report.exists()) return@doLast

        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("mac") -> listOf("open", report.absolutePath)
            os.contains("win") -> listOf("rundll32", "url.dll,FileProtocolHandler", report.absolutePath)
            else -> listOf("xdg-open", report.absolutePath)
        }
        ProcessBuilder(cmd).start()
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-platform-suite")
    }
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "--sun-misc-unsafe-memory-access=allow",
        "-Xshare:off"
    )

    finalizedBy(openCucumberReport)
}
