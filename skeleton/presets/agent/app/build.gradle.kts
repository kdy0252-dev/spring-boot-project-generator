import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.example.conventions.spring-web")
    id("com.example.conventions.spring-docs")
    id("com.example.conventions.openrewrite")
    id("com.example.conventions.errorprone")
    id("com.example.conventions.checkstyle")
    id("com.example.conventions.testcontainer")
    id("com.example.conventions.jmolecules")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(platform(libs.spring.modulith.bom))
    testImplementation(platform(libs.spring.modulith.bom))
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    runtimeOnly("org.springframework.modulith:spring-modulith-actuator")
    runtimeOnly("org.springframework.modulith:spring-modulith-observability")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    implementation(libs.embabel.agent.starter.openai)
    implementation(libs.vavr)
    testImplementation("jakarta.persistence:jakarta.persistence-api")
    testImplementation("org.springframework.data:spring-data-commons")
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
