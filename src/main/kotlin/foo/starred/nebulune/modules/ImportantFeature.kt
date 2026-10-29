@file:Suppress("Unused")

package foo.starred.nebulune.modules

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.modules.impl.ModSettings
import foo.starred.kontext.abstraction.chat.message.MinecraftMessages
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.text.parser.impl.parse
import kotlin.time.Duration.Companion.minutes

@LoadableAthenModule
object ImportantFeature {
    private val set = setOf("516m")

    val enabled by ModSettings.config.switch("Important feature", true)
    private val _enabled by ModSettings.config.information("Disabling the important feature may cause issues!")

    init {
        Scheduler.repeat(20.minutes) {
            if (client.level == null) return@repeat
            if (!enabled) return@repeat
            if ((0..100).random() > 4) return@repeat
            val a = if (set.size == 1) set.first() else set.random()

            MinecraftMessages.display("<red>[<orange>ዞ<red>] $a <yellow>joined the game.".parse())
        }
    }
}