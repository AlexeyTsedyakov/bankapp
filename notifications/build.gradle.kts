plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "notifications"

extra["springCloudVersion"] = "2025.1.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
