package foo.starred.nebulune.utils

import foo.starred.kontext.abstraction.client.extension.boundKey
import foo.starred.kontext.abstraction.client.extension.client
import net.minecraft.client.KeyMapping
import net.minecraft.world.entity.player.Player

fun rightClick() {
    val options = client.options
    val key = options.keyUse.boundKey()
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}

fun leftClick() {
    val options = client.options
    val key = options.keyAttack.boundKey()
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}

fun Player.getSkinTexture(): String? = gameProfile.properties.get("textures").firstOrNull()?.value