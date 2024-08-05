plugins {
    kotlin("jvm") version "2.0.0"
    id("jacoco")
}

jacoco {
    toolVersion = "[0.8.13-SNAPSHOT,)"
}

repositories {
    mavenLocal {
        mavenContent {
            snapshotsOnly()
            includeGroup("org.jacoco")
        }
    }
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
