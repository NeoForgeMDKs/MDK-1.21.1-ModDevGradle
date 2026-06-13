import org.gradle.plugins.ide.idea.model.IdeaModel

plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev").version("2.0.141")
    id("idea")
}

object ModConfig {
    const val MINECRAFT_VERSION = "1.21.1"
    const val MINECRAFT_VERSION_RANGE = "[1.21.1]"
    const val NEO_VERSION = "21.1.233"
    const val NEO_VERSION_RANGE = "[21.1,)"
    const val PARCHMENT_MAPPING_VERSION = "2024.11.17"
    const val LOADER_VERSION_RANGE = "[1,)"

    const val MOD_ID = "examplemod"
    const val MOD_NAME = "Example Mod"
    const val MOD_LICENSE = "MIT"
    const val MOD_VERSION = "1.0.0"
    const val MOD_GROUP_ID = "io.github.mochi_753.examplemod"
    const val MOD_DISPLAY_URL = "https://github.com/mochi-753/MDK-1.21.1-ModDevGradle"
    const val MOD_AUTHORS = "Mochi753"
    const val MOD_DESCRIPTION = "EXAMPLE MOD"
}

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

version = ModConfig.MOD_VERSION
group = ModConfig.MOD_GROUP_ID

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
    exclude("src/generated/**/.cache")
}

repositories {
    mavenCentral()
}

base {
    @Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT")
    archivesName.set(ModConfig.MOD_NAME.replace(' ', '-'))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

neoForge {
    version = ModConfig.NEO_VERSION

    parchment {
        mappingsVersion.set(ModConfig.PARCHMENT_MAPPING_VERSION)
        minecraftVersion.set(ModConfig.MINECRAFT_VERSION)
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", ModConfig.MOD_ID)
            gameDirectory.set(file("runs/client"))
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", ModConfig.MOD_ID)
            gameDirectory.set(file("runs/server"))
        }

        create("gameTestServer") {
            type = "gameTestServer"
            gameDirectory.set(file("runs/server"))
            systemProperty("neoforge.enabledGameTestNamespaces", ModConfig.MOD_ID)
        }

        create("data") {
            data()
            programArguments.addAll(
                "--mod",
                ModConfig.MOD_ID,
                "--all",
                "--output",
                file("src/generated/resources").absolutePath,
                "--existing",
                file("src/main/resources").absolutePath
            )
            gameDirectory.set(file("runs/data"))
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel.set(org.slf4j.event.Level.DEBUG)
        }
    }

    mods {
        create(ModConfig.MOD_ID) {
            sourceSet(sourceSets.main.get())
        }
    }
}

val localRuntime: Configuration by configurations.creating
configurations {
    runtimeClasspath {
        @Suppress("UnstableApiUsage")
        extendsFrom(localRuntime)
    }
}

dependencies {
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to ModConfig.MINECRAFT_VERSION,
        "minecraft_version_range" to ModConfig.MINECRAFT_VERSION_RANGE,
        "neo_version" to ModConfig.NEO_VERSION,
        "neo_version_range" to ModConfig.NEO_VERSION_RANGE,
        "loader_version_range" to ModConfig.LOADER_VERSION_RANGE,
        "mod_id" to ModConfig.MOD_ID,
        "mod_name" to ModConfig.MOD_NAME,
        "mod_license" to ModConfig.MOD_LICENSE,
        "mod_version" to ModConfig.MOD_VERSION,
        "mod_display_url" to ModConfig.MOD_DISPLAY_URL,
        "mod_authors" to ModConfig.MOD_AUTHORS,
        "mod_description" to ModConfig.MOD_DESCRIPTION
    )
    inputs.properties(replaceProperties)

    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main {
    resources.srcDir(generateModMetadata)
}

neoForge.ideSyncTask(generateModMetadata)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

plugins.withId("idea") {
    configure<IdeaModel> {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}
