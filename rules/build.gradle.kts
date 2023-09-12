plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktlint)
  `maven-publish`
}

dependencies {
  implementation(libs.ktlint.rule.engine.core)
  implementation(libs.ktlint.cli.ruleset.core)
  testImplementation(libs.ktlint.test)
  testImplementation(libs.slf4j)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn(tasks.classes)
  archiveClassifier.set("sources")
  from(sourceSets.main.map { it.allSource })
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn(tasks.javadoc)
  archiveClassifier.set("javadoc")
  from(tasks.javadoc.map { it.destinationDir!! })
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(sourcesJar)
      artifact(javadocJar)
      pom {
        description.set("YAZIO ktlint rules")
        url.set("https://github.com/yazio/YazioKtlintRules")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
      }
    }
  }
}

kotlin {
  jvmToolchain(libs.versions.java.get().toInt())
}
