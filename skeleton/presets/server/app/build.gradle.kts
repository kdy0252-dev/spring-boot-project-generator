import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.example.conventions.spring-web")
    id("com.example.conventions.spring-docs")
    id("com.example.conventions.spring-data")
    id("com.example.conventions.spring-modulith")
    id("com.example.conventions.spring-security")
    id("com.example.conventions.openrewrite")
    id("com.example.conventions.errorprone")
    id("com.example.conventions.checkstyle")
    id("com.example.conventions.testcontainer")
    id("com.example.conventions.jmolecules")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(libs.vavr)
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation(libs.h2.test)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-processing")
}

val architectureTestPatterns = listOf("*ArchTest", "*ArchitectureTest")

tasks.named<Test>("test") {
    filter {
        architectureTestPatterns.forEach(::excludeTestsMatching)
    }
}

val architectureTest = tasks.register<Test>("architectureTest") {
    description = "Runs architecture rule tests excluded from the default test task."
    group = "verification"
    maxParallelForks = 1
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    filter {
        architectureTestPatterns.forEach(::includeTestsMatching)
    }
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(architectureTest)
}

tasks.withType<BootJar>() {
    enabled = true
    entryCompression = ZipEntryCompression.STORED
}

tasks.getByName("jar") {
    enabled = false
}
