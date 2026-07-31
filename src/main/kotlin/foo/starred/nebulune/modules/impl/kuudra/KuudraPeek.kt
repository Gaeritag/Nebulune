package foo.starred.nebulune.modules.impl.kuudra

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.kuudra.KuudraAPI
import foo.starred.athen.api.kuudra.enums.KuudraPhase
import foo.starred.athen.api.kuudra.enums.KuudraTier
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.TickEvent
import foo.starred.athen.handlers.Typo.modMessage
import foo.starred.athen.modules.Module
import foo.starred.snowbird.api.client
import foo.starred.snowbird.handlers.parser.parse
import foo.starred.snowbird.utils.alert

@Load
@OnlyIn(islands = [SkyBlockIsland.KUUDRA])
object KuudraPeek : Module(
    "Kuudra peek",
    "Tries to detect which direction Kuudra will peek from!",
    Category.KUUDRA
) {
    private val y by config.switch("Check Y level", true)
    private val first by config.switch("Only show first peek")

    private var side: Side = Side.NONE

    private enum class Side {
        FRONT,
        BACK,
        RIGHT,
        LEFT,
        NONE;

        val str: String =
            name.lowercase().replaceFirstChar { it.uppercase() }
    }

    init {
        on<TickEvent.Client.End> {
            if (!KuudraAPI.inRun) return@on
            if (KuudraAPI.tier != KuudraTier.INFERNAL) return@on
            if (KuudraAPI.phase != KuudraPhase.Kill) return@on
            if (ticks % 2 != 0) return@on

            val player = client.player ?: return@on
            val kuudra = KuudraAPI.kuudra ?: return@on

            if (first && kuudra.health !in 24500f..25000f) return@on
            if (y && player.blockPosition().y >= 25) return@on

            val x = kuudra.blockPosition().x
            val z = kuudra.blockPosition().z

            val ns = when {
                x < -128 -> Side.RIGHT
                x > -72 -> Side.LEFT
                z > -84 -> Side.FRONT
                z < -132 -> Side.BACK
                else -> Side.NONE
            }

            if (side == ns) return@on
            side = ns

            "Kuudra peeked from <red>${side.str}<r>!".parse().modMessage()
            "<red>${side.name}!".parse().alert()
        }
    }
}