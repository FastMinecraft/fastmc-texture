import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.fastmc"
version = "0.0.1"

runVmOptions {
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
        "-XX:G1ReservePercent=15",
        "-XX:G1HeapWastePercent=10",
        "-XX:G1MixedGCCountTarget=16",
        "-XX:InitiatingHeapOccupancyPercent=50",
        "-XX:G1MixedGCLiveThresholdPercent=50",
        "-XX:G1RSetUpdatingPauseTimePercent=25",
        "-XX:G1OldCSetRegionThresholdPercent=5",
        "-XX:SurvivorRatio=5",
        "-XX:FlightRecorderOptions=stackdepth=512"
    )
}


plugins {
    id("dev.fastmc.mod-setup").version("1.3.0")
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://maven.luna5ama.dev/")
        maven("https://libraries.minecraft.net/")
    }

    dependencies {
        val kotlinVersion: String by rootProject
        val kotlinxCoroutineVersion: String by rootProject
        val jomlVersion: String by rootProject

        "libraryImplementation"("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        "libraryImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
        "libraryImplementation"("org.joml:joml:$jomlVersion")

        compileOnly("org.apache.logging.log4j:log4j-api:2.8.1")
        compileOnly("it.unimi.dsi:fastutil:7.1.0")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlin.contracts.ExperimentalContracts",
                    "-Xbackend-threads=0"
                )
            }
        }
    }
}

tasks {
    val collectJars by register<Copy>("collectJars") {
        group = "build"

        from(
            provider {
                subprojects.mapNotNull { it.tasks.findByName("modLoaderJar")?.outputs }
            }
        )

        into(file("${layout.buildDirectory.asFile.get()}/libs"))
    }

    assemble {
        finalizedBy(collectJars)
    }
}