@file:Suppress("ObjectPrivatePropertyName", "Unused")

package foo.starred.nebulune.modules.impl.general

import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.MessageEvent
import foo.starred.athen.events.WorldRenderEvent
import foo.starred.athen.modules.Module
import foo.starred.kontext.abstraction.chat.command.MinecraftCommands
import foo.starred.kontext.abstraction.title.MinecraftTitle
import foo.starred.kontext.api.entity.extension.renderAABB
import foo.starred.kontext.api.entity.extension.renderVec3
import foo.starred.kontext.api.format.extension.component.literal
import foo.starred.kontext.api.format.extension.string.stripped
import foo.starred.kontext.api.format.extension.time.parseDuration
import foo.starred.kontext.api.scheduling.scheduler.extensions.clientTicks
import foo.starred.kontext.api.text.parser.impl.parse
import foo.starred.cascade.primitives.impl.TextPrimitive
import foo.starred.cascade.wrappers.text.impl.CascadeTextWrapper
import foo.starred.nebulune.utils.extractTracer
import foo.starred.parallax.api.primitives.ParallaxBox

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

@LoadableAthenModule
@AthenModuleScope(islands = [PresetSkyBlockIsland.THE_BARN])
object TrevorHelper : Module(
    "Trevor helper",
    "Helper features for Trevor the Trapper!",
    ConfigCategory.GENERAL
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
        if (false) { TextPrimitive.text { wrapper = CascadeTextWrapper; text = "Cooldown: §c12.4s".literal() }; return@hud }
        if (cooldown <= 0) return@hud
        val t = ((cooldown - System.currentTimeMillis()).coerceAtLeast(0) / 1000.0).parseDuration(decimals = 1)

        TextPrimitive.text { wrapper = CascadeTextWrapper; text = "Cooldown: §c$t".literal() }
    }

    private val colors by config.group("Colors")
    private val `color$trackable` by colors.colorPicker("Trackable color", MochaColorScheme.Text.argb)
    private val `color$untrackable` by colors.colorPicker("Untrackable color", MochaColorScheme.Green.argb)
    private val `color$undetected` by colors.colorPicker("Undetected color", MochaColorScheme.Blue.argb)
    private val `color$endangered` by colors.colorPicker("Endangered color", MochaColorScheme.Mauve.argb)
    private val `color$elusive` by colors.colorPicker("Elusive color", MochaColorScheme.Yellow.argb)

    private val animals = setOf(Cow::class, Pig::class, Sheep::class, Chicken::class, Rabbit::class, Horse::class)
    private val startRegex = Regex("\\[NPC] Trevor: You can find your (?<type>\\w+) animal near the .*")

    private var cooldown: Long = 0
    private var rarity: Rarity? = null

    init {
        on<LocationEvent.Server.Connect> {
            reset()
        }

        on<WorldRenderEvent.Entity> {
            if (!mobEsp) return@on

            val rarity = rarity ?: return@on
            val entity = entity as? LivingEntity ?: return@on
            if (entity::class !in animals) return@on

            val max = if (entity is Horse) entity.serverMaxHealth / 2f else entity.serverMaxHealth
            if (max != rarity.hp) return@on

            ParallaxBox.frame(entity.renderAABB(), rarity.color, depth = false)
            if (`esp$tracer`) extractTracer(entity.renderVec3(), rarity.color)
        }

        on<MessageEvent.Chat.Receive> {
            startRegex.findThenNull(stripped, "type") { (t) ->
                rarity = Rarity.get(t) ?: return@findThenNull
                cooldown = System.currentTimeMillis() + 20_000

                Scheduler.schedule(20.seconds) {
                    `alert$sound`.play()
                    if (endAlert) MinecraftTitle.display(`alert$message`.parse())
                    cooldown = 0
                }
            } ?: return@on

            if (stripped == "Return to the Trapper soon to get a new animal to hunt!") {
                if (!autoCall) return@on reset()

                val ms = (cooldown - System.currentTimeMillis() - (callOff * 1000).toLong()).coerceAtLeast(0)
                val extra = (callDelay + (0..2).random()) * 50L

                Scheduler.schedule((ms + extra).milliseconds) { MinecraftCommands.client("/call trevor") }
                return@on reset()
            }

            if (!autoAccept) return@on
            if (message.siblings.getOrNull(0)?.stripped() == "Accept the trapper's task to hunt the animal?") {
                Scheduler.schedule((acceptDelay + (0..2).random()).clientTicks) {
                    (message.siblings[3].style.clickEvent as? ClickEvent.RunCommand)?.command?.let { MinecraftCommands.client(it) }
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

        val color: Int
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