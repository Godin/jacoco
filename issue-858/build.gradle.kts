plugins {
    java
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    configure<JacocoPluginExtension> {
        toolVersion = "0.8.3"
//        toolVersion = "0.8.2"
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_10
        targetCompatibility = JavaVersion.VERSION_1_10
    }

    afterEvaluate {
        tasks.named<JavaCompile>("compileJava") {
            options.compilerArgs.clear()
            options.compilerArgs.addAll(arrayOf(
                    "--module-path", classpath.asPath
            ))
            classpath = files()
        }
    }
}

rootProject.apply {
    tasks {
        register<JacocoReport>("jacocoRootReport") {
            executionData.from(
                    file("b/build/jacoco/test.exec")
            )
            sourceDirectories.from(
                    files(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
            )
            classDirectories.from(
                    files(subprojects.map {

                        it.sourceSets.main.get().output
//                                .asFileTree.matching { exclude("module-info.class") }

                    })
            )
        }
    }
}
