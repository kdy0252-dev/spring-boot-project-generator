package com.example.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

class SpringJavaConventions : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply("java")
        plugins.apply("jacoco")
        group = "com.example.conventions"

        extensions.configure(JavaPluginExtension::class.java) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        }

        extensions.configure(JacocoPluginExtension::class.java) {
            toolVersion = "0.8.15"
        }

        val coverageExclusions = listOf(
            "**/*Application.class",
            "**/*ApplicationKt.class",
            "**/*Config.class",
            "**/*Configuration.class",
            "**/config/**",
            "**/generated/**",
            "**/global/annotation/**"
        )
        val coverageClassDirectories = provider {
            extensions.getByType<JavaPluginExtension>()
                .sourceSets
                .getByName("main")
                .output
                .asFileTree
                .matching {
                    exclude(*coverageExclusions.toTypedArray())
                }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            add("testImplementation", libs.findLibrary("assertj-core").get())
            add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            jvmArgs("-Dspring.test.context.cache.maxSize=8")
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }

        tasks.named<Test>("test") {
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        tasks.named<JacocoReport>("jacocoTestReport") {
            dependsOn(tasks.named("test"))
            classDirectories.setFrom(coverageClassDirectories)
            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }
        }

        tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
            dependsOn(tasks.named("test"))
            classDirectories.setFrom(coverageClassDirectories)
            violationRules {
                rule {
                    limit {
                        counter = "LINE"
                        value = "COVEREDRATIO"
                        minimum = "0.70".toBigDecimal()
                    }
                }
            }
        }

        tasks.named("check") {
            dependsOn(tasks.named("jacocoTestCoverageVerification"))
        }
    }
}
