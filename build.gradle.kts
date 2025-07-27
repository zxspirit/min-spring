plugins {
    id("java")
}

group = "com.newzhxu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.apache.tomcat.embed/tomcat-embed-core
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.9")

    // https://mvnrepository.com/artifact/org.slf4j/jul-to-slf4j
    implementation("org.slf4j:jul-to-slf4j:2.0.17")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.13.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}


tasks.test {
    useJUnitPlatform()
}
// 暂时不能使用jar启动  ,由于遍历方式问题
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.newzhxu.Main"
    }
}