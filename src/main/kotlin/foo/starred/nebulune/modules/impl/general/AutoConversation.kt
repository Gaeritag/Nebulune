package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.MessageEvent
import foo.starred.athen.modules.Module
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.snowbird.api.scheduling.scheduler.extensions.start
import foo.starred.snowbird.utils.colorCoded
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

@Load
@OnlyIn(skyblock = true)
object AutoConversation : Module(
    "Auto conversation",
    "Automatically has a conversation with NPCs!",
    ConfigCategory.GENERAL
) {
    private val multi by config.switch("Multi-option dialogues", true)
    private val green by config.switch("Check green color", true)
    private val delay by config.slider("Click delay", 4, 0, 40, "ticks")

    init {
        on<MessageEvent.Chat.Receive> {
            if (!stripped.startsWith("[NPC] ") && !stripped.startsWith("Select an option: ")) return@on
            val a = mutableListOf<ClickEvent.Custom>()
            val b = green

            val queue = mutableListOf(message)
            var i = 0

            while (i < queue.size) {
                val current = queue[i++]
                queue.addAll(current.siblings)

                val clickEvent = current.style.clickEvent as? ClickEvent.Custom ?: continue
                if (!b || current.style.color?.value == TextColor.GREEN || current.colorCoded().contains("§a")) {
                    a.add(clickEvent)
                }
            }

            if (a.isEmpty()) return@on
            if (a.size > 1 && !multi) return@on
            if (delay == 0) return@on a.first().sendClickPacket()

            Scheduler.schedule((delay + (0..3).random()).clientTicks.start) {
                a.first().sendClickPacket()
            }
        }
    }

    private fun ClickEvent.Custom.sendClickPacket() {
        val packet = ServerboundCustomClickActionPacket(this.id(), this.payload())
        Minecraft.getInstance().connection?.send(packet)
    }
}