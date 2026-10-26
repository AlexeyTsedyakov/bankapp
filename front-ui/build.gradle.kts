plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "front-ui"

extra["springCloudVersion"] = "2025.1.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.htmx.spring.boot)
    implementation(libs.htmx.spring.boot.thymeleaf)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-thymeleaf-test")
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