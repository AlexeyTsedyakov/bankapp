plugins {
    java
    id("org.springframework.boot") version "4.1.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "org.example"
    version = "v1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.apply("java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    // Оставляем только fat-jar при сборке
    tasks.jar {
        enabled = false
    }

    // Общие настройки для всех задач типа Test (и test, и integrationTest)
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // Обычный test — без интеграционных тестов
    tasks.named<Test>("test") {
        useJUnitPlatform {
            excludeTags("integration")
        }
    }

    // Отдельная задача для интеграционных тестов
    val testSourceSet = extensions.getByType<SourceSetContainer>().named("test")
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = LifecycleBasePlugin.VERIFICATION_GROUP

        testClassesDirs = testSourceSet.get().output.classesDirs
        classpath = testSourceSet.get().runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }
        shouldRunAfter(tasks.named("test"))
    }
}