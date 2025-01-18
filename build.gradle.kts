// https://start.spring.io/
plugins {
    java
    id("org.springframework.boot") version "3.3.8-SNAPSHOT"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hibernate.orm") version "6.5.3.Final"
    id("org.graalvm.buildtools.native") version "0.10.4"
}

group = "com.jeka8833"
version = "1.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven {
        url = uri("https://maven.pkg.github.com/TNTClient/TOProtocol")
        credentials {
            username = project.property("tntclient.username") as String?
            password = project.property("tntclient.password") as String?
        }
    }
}

extra["springCloudGcpVersion"] = "5.9.0"
extra["springCloudVersion"] = "2023.0.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.google.cloud:spring-cloud-gcp-starter-vision")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // https://mvnrepository.com/artifact/com.github.loki4j/loki-logback-appender
    implementation("com.github.loki4j:loki-logback-appender:1.6.0")

    // https://mvnrepository.com/artifact/com.github.loki4j/loki-protobuf
    implementation("com.github.loki4j:loki-protobuf:0.0.2_pb4.28.0")

    // https://mvnrepository.com/artifact/org.codehaus.janino/janino
    implementation("org.codehaus.janino:janino:3.1.12")

    // https://mvnrepository.com/artifact/io.micrometer/micrometer-core
    implementation("io.micrometer:micrometer-core")

    // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
    implementation("com.squareup.okhttp3:okhttp")

    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.2.2"){
        exclude(module = "opus-java")
    }

    // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")

    // https://mvnrepository.com/artifact/com.bucket4j/bucket4j_jdk17-core
    implementation("com.bucket4j:bucket4j_jdk17-core:8.14.0")

    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:26.0.1")

    implementation("com.jeka8833:toprotocol:+")
}

dependencyManagement {
    imports {
        mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:${property("springCloudGcpVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

hibernate {
    enhancement {
        enableAssociationManagement = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass = "com.jeka8833.tntclientendpoints.RunAllServices"
}
