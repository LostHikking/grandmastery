plugins {
    id("java")
    id("jacoco")
    id("net.ltgt.errorprone") version "3.1.0"
    id("checkstyle")
    id("io.freefair.lombok") version "8.2.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.deeplay.grandmastery"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")
    apply(plugin = "net.ltgt.errorprone")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "com.github.johnrengelman.shadow")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.7")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
        implementation("ch.qos.logback:logback-classic:1.4.11")

        errorprone("com.google.errorprone:error_prone_core:2.20.0")

        testImplementation(platform("org.junit:junit-bom:5.9.3"))
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
        testImplementation("org.mockito:mockito-core:5.2.0")
    }

    checkstyle {
        toolVersion = "10.3.3"
    }

    tasks.withType<Checkstyle>().configureEach {
        maxErrors = 0
        maxWarnings = 0
        reports {
            html.required.set(true)
            xml.required.set(false)
        }
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(false)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        systemProperty("file.encoding", "UTF-8")
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    tasks.shadowJar {
        dependencies {
            exclude(dependency("org.nd4j:.*:.*"))
            exclude(dependency("org.bytedeco:.*:.*"))
        }
    }

    tasks.build {
        dependsOn(tasks.shadowJar)
    }
}
