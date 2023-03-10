import me.luna.jaroptimizer.JarOptimizerPluginExtension
import kotlin.math.max

group = "dev.fastmc"
version = "0.0.1"

runVmOptions {
    val threads = Runtime.getRuntime().availableProcessors()
    add(
        "-Xms2G",
        "-Xmx2G",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+AlwaysPreTouch",
        "-XX:+ExplicitGCInvokesConcurrent",
        "-XX:+ParallelRefProcEnabled",
        "-XX:+UseG1GC",
        "-XX:+UseStringDeduplication",
        "-XX:MaxGCPauseMillis=1",
        "-XX:G1NewSizePercent=2",
        "-XX:G1MaxNewSizePercent=10",
        "-XX:G1HeapRegionSize=1M",
        "-XX:G1ReservePercent=15",
        "-XX:G1HeapWastePercent=10",
        "-XX:G1MixedGCCountTarget=16",
        "-XX:InitiatingHeapOccupancyPercent=50",
        "-XX:G1MixedGCLiveThresholdPercent=50",
        "-XX:G1RSetUpdatingPauseTimePercent=25",
        "-XX:G1OldCSetRegionThresholdPercent=5",
        "-XX:SurvivorRatio=5",
        "-XX:ParallelGCThreads=$threads",
        "-XX:ConcGCThreads=${max(threads / 4, 1)}",
        "-XX:FlightRecorderOptions=stackdepth=512"
    )
}

plugins {
    id("me.luna.jaroptimizer").version("1.1")
    id("dev.fastmc.modsetup.root").version("1.0-SNAPSHOT")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.fastmc.dev/")
        maven("https://libraries.minecraft.net/")
    }

    dependencies {
        val jomlVersion: String by rootProject

        libraryImplementation("org.joml:joml:$jomlVersion")

        compileOnly("org.apache.logging.log4j:log4j-api:2.8.1")
        compileOnly("it.unimi.dsi:fastutil:7.1.0")
    }
}

subprojects {
    tasks {
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlin.contracts.ExperimentalContracts",
                )
            }
        }
    }
}

tasks {
    val collectJars by register<Copy>("collectJars") {
        finalizedBy("optimizeJars")

        group = "build"

        from(
            provider {
                subprojects.mapNotNull { it.tasks.findByName("releaseJar")?.outputs }
            }
        )

        into(file("$buildDir/libs"))
    }

    configure<JarOptimizerPluginExtension> {
        add(collectJars, "dev.fastmc", "org.spongepowered")
    }

    assemble {
        finalizedBy(collectJars)
    }
}