plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.loom) apply false
}

stonecutter active "26.1"

val mod = extensions.getByType<VersionCatalogsExtension>().named("mod")

stonecutter parameters {
    swaps["mod_version"] = "\"${mod("version")}\""
    swaps["mod_id"] = "\"${mod("id")}\""
    swaps["mod_name"] = "\"${mod("name")}\""
    swaps["minecraft"] = "\"${node.metadata.version}\""

    swaps["layout"] = if (current.parsed >= "26.3") "layout(location = $1) $2 " else "$2 "

    replacements {
        string(current.parsed >= "26.3") {
            replace("#moj_import", "#include")
        }
    }
}

operator fun VersionCatalog.invoke(name: String): String {
    return findVersion(name).get().requiredVersion
}