plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.loom) apply false
}

stonecutter active "26.1"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\""
    swaps["mod_id"] = "\"" + property("mod.id") + "\""
    swaps["minecraft"] = "\"" + node.metadata.version + "\""
}