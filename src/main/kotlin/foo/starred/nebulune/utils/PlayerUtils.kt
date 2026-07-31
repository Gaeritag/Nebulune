package foo.starred.nebulune.utils

import foo.starred.athen.mixin.accessors.KeyMappingAccessor
import foo.starred.snowbird.api.client
import net.minecraft.client.KeyMapping

fun rightClick() {
    val options = client.options ?: return
    val key = (options.keyUse as KeyMappingAccessor).boundKey
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}

fun leftClick() {
    val options = client.options ?: return
    val key = (options.keyAttack as KeyMappingAccessor).boundKey
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}