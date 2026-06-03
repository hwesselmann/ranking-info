plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.4.0"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
    id("dev.detekt") version "2.0.0-alpha.3"
    id("org.jetbrains.dokka") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.sonarqube") version "7.3.0.8198"
}

group = "de.hdawg.tennis"
version = "0.0.1-SNAPSHOT"
description = "ranking-info-kt"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    implementation("org.jetbrains.exposed:exposed-spring-boot4-starter:1.3.0")
    implementation("org.jetbrains.exposed:exposed-java-time:1.3.0")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.postgresql:postgresql:42.7.11")
    runtimeOnly("org.xerial:sqlite-jdbc:3.53.1.0")

    // Pagination types (Page<T>, PageImpl, PageRequest)
    implementation("org.springframework.data:spring-data-commons")

    // CSV parsing
    implementation("com.opencsv:opencsv:5.12.0")

    // API docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // Rate limiting
    implementation("com.bucket4j:bucket4j_jdk17-core:8.19.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.named("check") {
    dependsOn("ktlintCheck", "detekt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

kover {
    reports {
        total {
            xml { onCheck = true }
            html { onCheck = true }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    baseline = file("detekt-baseline.xml")
}

sonar {
    properties {
        property("sonar.projectKey", "hwesselmann_ranking-info2")
        property("sonar.organization", "hwesselmann")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")
    }
}
