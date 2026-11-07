plugins {
    java
    alias(libs.plugins.shadow)
}

group = "me.ddggdd135.slimeae"
version = project.property("version").toString()

java {
    // 不钉 toolchain：本机/CI JDK 版本可能不同，统一由 compileJava 的 options.release 控制目标
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    // Core
    compileOnly(libs.paper.api)
    compileOnly(libs.slimefun4)
    compileOnly(libs.cmilib)
    compileOnly(libs.gugu.slimefun.lib)
    compileOnly(libs.guizhan.lib.plugin)
    compileOnly(libs.pinyin)
    compileOnly(libs.sqlite.jdbc)

    // Tools etc.（打包进 jar）
    implementation(libs.bstats)
    implementation(libs.hikaricp)
    implementation(libs.libby.bukkit)

    // Supported Plugins
    compileOnly(libs.infinity.expansion)
    compileOnly(libs.just.enough.guide)
    // System-scoped local JARs（蓬松/网络拓展/末地科技/星系/黑曜石/异域花园/逻辑/乱序）
    compileOnly(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations {
    // Maven 下 provided 会进 test classpath，Gradle 默认不带；单测引用了 compileOnly 的 Slimefun/GuguLib 类型
    testCompileClasspath { extendsFrom(configurations.compileOnly.get()) }
    testRuntimeClasspath { extendsFrom(configurations.compileOnly.get()) }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.stdout.encoding", "UTF-8")
    systemProperty("sun.stderr.encoding", "UTF-8")
}

tasks {
    compileJava {
        options.compilerArgs.add("-Xlint:-removal")
        // paper-api 1.20.1 为 Java 17 字节码，且 1.20.1 运行时要求 17，目标从原 16 升至 17
        options.release = 17
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version.toString())
        }
    }

    shadowJar {
        archiveBaseName.set("SlimeAE")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        minimize()

        // Relocations（与原 pom 一致）
        relocate("org.bstats", "me.ddggdd135.slimeae.libs.bstats")
        relocate("com.zaxxer.hikari", "me.ddggdd135.slimeae.libs.hikari")
        relocate("net.byteflux.libby", "me.ddggdd135.slimeae.libs.libby")

        exclude("META-INF/*")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}

// Set default tasks
defaultTasks("clean", "build")
