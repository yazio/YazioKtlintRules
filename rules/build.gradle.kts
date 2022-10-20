plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(libs.ktlint.core)
  testImplementation(libs.ktlint.test)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.params)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}
