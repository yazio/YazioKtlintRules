plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlinter)
}

buildscript {
  configurations.classpath {
    resolutionStrategy {
      eachDependency {
        if (requested.group == "com.pinterest.ktlint") {
          useVersion(libs.versions.ktlint.get())
        }
      }
    }
  }
}
