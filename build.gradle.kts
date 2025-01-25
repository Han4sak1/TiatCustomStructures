import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.BukkitUtil
import io.izzel.taboolib.gradle.Database


plugins {
    java
    id("io.izzel.taboolib") version "2.0.22"
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
}

taboolib {
    env {
        install(Basic)
        install(Bukkit)
        install(BukkitUtil)
        install(Database)
        install(CommandHelper)
    }
    description {
        dependencies {
            name("WorldEdit")
            name("FastAsyncWorldEdit").optional(true)
            name("PlaceholderAPI").optional(true)
            name("Adyeshach").optional(true)
            name("MythicMobs").optional(true)
            name("OpenTerrainGenerator").optional(true)
        }
    }
    version { taboolib = "6.2.2" }

    relocate("ink.ptms.um", "me.gei.tiatcustomstructures.api.compact.mythic.um")
}

repositories {
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    mavenCentral()
}

dependencies {
    taboo("ink.ptms:um:1.1.2")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
