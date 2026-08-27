@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.rendering.level.impl.extensions.impl.extractFrameBox
import foo.starred.athen.api.rendering.ui.text.vanilla.extensions.sizedText
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.Category
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.MessageEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.athen.ui.themes.Catppuccin
import foo.starred.athen.utils.render.renderBoundingBox
import foo.starred.athen.utils.render.renderPos
import foo.starred.nebulune.utils.extractTracer
import foo.starred.snowbird.api.command
import foo.starred.snowbird.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.snowbird.handlers.parser.parse
import foo.starred.snowbird.utils.alert
import foo.starred.snowbird.utils.stripped
import foo.starred.snowbird.utils.toDurationFromMillis
import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.chicken.Chicken
import net.minecraft.world.entity.animal.cow.Cow
import net.minecraft.world.entity.animal.equine.Horse
import net.minecraft.world.entity.animal.pig.Pig
import net.minecraft.world.entity.animal.rabbit.Rabbit
import net.minecraft.world.entity.animal.sheep.Sheep
import tech.thatgravyboat.skyblockapi.api.data.MayorCandidates
import tech.thatgravyboat.skyblockapi.utils.extentions.serverMaxHealth
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findThenNull
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_BARN])
object TrevorHelper : Module(
    "Trevor helper",
    "Helper features for Trevor the Trapper!",
    Category.GENERAL
) {
    private val mobEsp by config.switch("Animal ESP")
    private val `esp$tracer` by config.switch("Show tracer")

    private val autoCall by config.switch("Auto call")
    private val callDelay by config.slider("Call delay", 1, 0, 5, "ticks")
    private val callOff by config.slider("Call early", 2.0, 0.0, 5.0, "seconds", true)

    private val autoAccept by config.switch("Auto accept")
    private val acceptDelay by config.slider("Accept delay", 1, 0, 5, "ticks")

    private val endAlert by config.switch("Cooldown end alert")
    private val `alert$message` by config.input("Alert message", "<red>Cooldown ended!")
    private val `alert$sound` by config.sound("Alert sound")

    private val hud = config.hud("Cooldown timer") {
        if (it) return@hud sizedText("Cooldown: §c12.4s")
        if (cooldown <= 0) return@hud null
        val t = (cooldown - System.currentTimeMillis()).coerceAtLeast(0).toDurationFromMillis(secondsDecimals = 1)

        sizedText("Cooldown: §c$t")
    }

    private val colors by config.group("Colors")
    private val `color$trackable` by colors.colorPicker("Trackable color", Color(Catppuccin.Mocha.Text.argb, true))
    private val `color$untrackable` by colors.colorPicker("Untrackable color", Color(Catppuccin.Mocha.Green.argb, true))
    private val `color$undetected` by colors.colorPicker("Undetected color", Color(Catppuccin.Mocha.Blue.argb, true))
    private val `color$endangered` by colors.colorPicker("Endangered color", Color(Catppuccin.Mocha.Mauve.argb, true))
    private val `color$elusive` by colors.colorPicker("Elusive color", Color(Catppuccin.Mocha.Yellow.argb, true))

    private val animals = setOf(Cow::class, Pig::class, Sheep::class, Chicken::class, Rabbit::class, Horse::class)
    private val startRegex = Regex("\\[NPC] Trevor: You can find your (?<type>\\w+) animal near the .*")

    private var cooldown: Long = 0
    private var rarity: Rarity? = null

    init {
        on<LocationEvent.Server.Connect> {
            reset()
        }

        on<WorldRenderEvent.Entity.Post> {
            if (!mobEsp) return@on

            val rarity = rarity ?: return@on
            val entity = entity as? LivingEntity ?: return@on
            if (entity::class !in animals) return@on

            val max = if (entity is Horse) entity.serverMaxHealth / 2f else entity.serverMaxHealth
            if (max != rarity.hp) return@on

            extractFrameBox(entity.renderBoundingBox, rarity.color.rgb, depth = false)
            if (`esp$tracer`) extractTracer(entity.renderPos, rarity.color.rgb)
        }

        on<MessageEvent.Chat.Receive> {
            startRegex.findThenNull(stripped, "type") { (t) ->
                rarity = Rarity.get(t) ?: return@findThenNull
                cooldown = System.currentTimeMillis() + 20_000

                Scheduler.schedule(20.seconds) {
                    if (endAlert) `alert$message`.parse().alert(soundType = `alert$sound`.sound)
                    cooldown = 0
                }
            } ?: return@on

            if (stripped == "Return to the Trapper soon to get a new animal to hunt!") {
                if (!autoCall) return@on reset()

                val ms = (cooldown - System.currentTimeMillis() - (callOff * 1000).toLong()).coerceAtLeast(0)
                val extra = (callDelay + (0..2).random()) * 50L

                Scheduler.schedule((ms + extra).milliseconds) { "/call trevor".command() }
                return@on reset()
            }

            if (!autoAccept) return@on
            if (message.siblings.getOrNull(0)?.stripped() == "Accept the trapper's task to hunt the animal?") {
                Scheduler.schedule((acceptDelay + (0..2).random()).clientTicks) {
                    (message.siblings[3].style.clickEvent as? ClickEvent.RunCommand)?.command?.command()
                }

                return@on
            }
        }
    }

    private fun reset() {
        rarity = null
    }

    private enum class Rarity(val normal: Float, val derpy: Float) {
        Trackable(100f, 200f),
        Untrackable(500f, 1000f),
        Undetected(1000f, 2000f),
        Endangered(5000f, 10000f),
        Elusive(10000f, 20000f);

        val hp: Float
            get() = if (MayorCandidates.DERPY.isActive) derpy else normal

        val color: Color
            get() = when (this) {
                Trackable -> `color$trackable`
                Untrackable -> `color$untrackable`
                Undetected -> `color$undetected`
                Endangered -> `color$endangered`
                Elusive -> `color$elusive`
            }

        companion object {
            fun get(a: String): Rarity? = entries.find { it.name.uppercase() == a }
        }
    }
}