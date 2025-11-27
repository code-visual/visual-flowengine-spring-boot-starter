import java.net.URI

plugins {
    `java-library`
    groovy
    `maven-publish`
    signing
}

group = "io.github.code-visual"
version = "1.0.8"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.apache.groovy:groovy:4.0.15")
    api("org.springframework.boot:spring-boot-starter-thymeleaf:2.7.11")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor:2.6.3")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.3")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("org.springframework:spring-webmvc:5.2.25.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.5") {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter-api")
    }
    testImplementation("junit:junit:4.12")
    testImplementation("org.testng:testng:6.14.3")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("visual-flowengine Application")
                description.set("visual-flowengine")
                url.set("https://www.oxooxx.com")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("levi")
                        name.set("levi")
                        email.set("levi.lideng@gmail.com")
                        organization.set("code-visual")
                        organizationUrl.set("https://www.oxooxx.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:code-visual/visual-flowengine-spring-boot-starter.git")
                    developerConnection.set("scm:git@github.com:code-visual/visual-flowengine-spring-boot-starter.git")
                    url.set("https://github.com/code-visual/visual-flowengine-spring-boot-starter")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                URI("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

tasks.register("addCopyrightHeaders") {
    doLast {
        val copyrightHeader = """
        /*
         * Copyright (c) 2023-2024, levi li (levi.lideng@gmail.com)
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *      https://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */
        """.trimIndent()

        listOf("src/main/java", "src/main/groovy").forEach { srcDir ->
            file(srcDir).walk().forEach { file ->
                if (file.isFile && (file.extension == "java" || file.extension == "groovy")) {
                    val content = file.readText()
                    if (!content.contains("Copyright")) {
                        file.writeText("$copyrightHeader\n$content")
                    }
                }
            }
        }
    }
}

tasks.named("build") {
    dependsOn("addCopyrightHeaders")
}
