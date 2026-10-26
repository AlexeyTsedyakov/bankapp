plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "api-gateway"

extra["springCloudVersion"] = "2025.1.3"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
