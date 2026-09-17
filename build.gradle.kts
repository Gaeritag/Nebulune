import net.fabricmc.loom.task.ValidateAccessWidenerTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.loom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingTable)
    `maven-publish`
}

val catalogs = extensions.getByType<VersionCatalogsExtension>()
val minecraft = stonecutter.current.version
val lib = catalogs.named("libs${minecraft.replace(".", "")}")
val mod = catalogs.named("mod")

version = "${mod("version")}+$minecraft"
group = mod("group")
base.archivesName = mod("id")

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.hypixel.net/repository/Hypixel")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.teamresourceful.com/repository/maven-public/")

    maven("https://maven.starred.foo/releases")
    maven("https://maven.starred.foo/snapshots")

    flatDir {
        dirs(rootProject.file("libs"))
    }
}

dependencies {
    minecraft(lib["minecraft"])

    localRuntime(libs.devauth)
    compileOnly(lib["entityculling"])

    compileOnly(lib["caxton"])
    compileOnly(lib["exordium"])
    compileOnly(lib["iris"])

    implementation(lib["modmenu"])
    implementation(lib["fabric-api"])
    implementation(libs.fabric.loader)
    implementation(libs.fabric.language.kotlin)
    implementation(libs.hypixel.modapi)
    implementation(libs.hypixel.modapi.fabric)

    implementation(libs.classgraph)
    implementation(libs.kommand)
    implementation(lib["snowbird"])
    implementation(lib["cascade"])

    implementation(libs.skyblock.api) {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-$minecraft") }
    }

    implementation(lib["athen"])
}

fletchingTable {
    mixins.create("main", Action {
        mixin("default", "${mod("id")}.mixins.json") {
            env("CLIENT")
        }
    })
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    val awFile = sc.process(
        rootProject.file("src/main/resources/${mod("id")}.classtweaker"),
        "build/${mod("tweaker")}"
    )

    if (awFile.exists()) {
        accessWidenerPath = awFile
    } else {
        println("Accesswidener source not found at src/main/resources/${mod("id")}.classtweaker")
    }

    runConfigs.named("client") {
        generateRunConfig = true
        jvmArguments.addAll("-Ddevauth.enabled=true", "-Ddevauth.account=main", "-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
    }

    runConfigs.named("server") {
        generateRunConfig = false
    }
}

java {
    withSourcesJar()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution", "-Xcollection-literals", "-Xskip-prerelease-check")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

tasks {
    processResources {
        val r = mapOf(
            "id" to mod("id"),
            "name" to mod("name"),
            "version" to mod("version"),
            "minecraft" to lib("compatibility"),
            "tweaker" to mod("tweaker")
        )

        inputs.properties(r)
        filesMatching("fabric.mod.json") { expand(r) }
        exclude("${mod("id")}.classtweaker")
    }

    register<Copy>("buildAndCollect") {
        description = "Builds and collects mod jars."
        group = "build"
        from(jar, kotlinSourcesJar)
        into(rootProject.layout.buildDirectory.file("libs/${mod("version")}"))
        dependsOn("build")
    }
}

tasks.withType<ValidateAccessWidenerTask>().configureEach {
    dependsOn("stonecutterPrepare")
}

fun DependencyHandlerScope.shadow(dep: Any, config: ExternalModuleDependency.() -> Unit = {}) {
    val d = create((dep as? Provider<*>)?.get() ?: dep) as ExternalModuleDependency
    d.config()
    include(d)
    implementation(d)
}

operator fun VersionCatalog.get(name: String): Provider<MinimalExternalModuleDependency> {
    return findLibrary(name).get()
}

operator fun VersionCatalog.invoke(name: String): String {
    return findVersion(name).get().requiredVersion
}