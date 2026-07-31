@file:Suppress("Unused")

package foo.starred.nebulune.modules.impl.slayer

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.api.slayers.SlayerAPI
import foo.starred.athen.api.slayers.enums.type.impl.SlayerBoss
import foo.starred.athen.config.Category
import foo.starred.athen.events.PlayerEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.modules.Module
import foo.starred.athen.utils.enchants
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.api.held
import net.minecraft.world.phys.EntityHitResult
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.profile.StatsAPI

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_END])
object AutoSoulcry : Module(
    "Auto soulcry",
    "Automatically uses the soulcry ability of your katana!",
    Category.SLAYER
) {
    private val mana by config.switch("Check mana", true)
    private val hitbox by config.switch("Check boss hitbox", true)
    private val minDelay by config.slider("Min delay", 1, 0, 5, "ticks")
    private val maxDelay by config.slider("Max delay", 3, 0, 5, "ticks")
    private val detectType = config.multiCheckbox("Detection type", listOf("Tick based", "Attack based")).custom("detectType")
    private val otherBosses by config.switch("Work on other's bosses")
    private val _unused0 by config.textParagraph("The option <red>\"Work on other's bosses\"<r> requires you to have <red>Attack based<r> selected in Detection type!")

    private val ids = setOf("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA")
    private var tick = -1

    init {
        on<TickEvent.Client.Start> {
            val slayer = SlayerAPI.slayer
            if (slayer?.type as? SlayerBoss != SlayerBoss.Voidgloom) return@on reset()
            if (client.screen != null) return@on reset()

            val item = held ?: return@on reset()
            if (item.getData(DataTypes.ID) !in ids) return@on reset()
            if (hitbox && client.hitResult as? EntityHitResult != slayer.entity) return@on

            val m = if ("ultimate_wise" in item.enchants()) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on reset()

            if (tick == -1) return@on ::tick.set((minDelay..maxDelay.coerceAtLeast(minDelay)).random())
            if (tick-- > 0) return@on

            rightClick()
            reset()
        }.runWhen(detectType.state.map { 0 in it })

        on<PlayerEvent.Attack.Entity> {
            val si = SlayerAPI.bosses[entity] ?: return@on
            if (!si.owned && !otherBosses) return@on

            val item = held ?: return@on
            if (item.getData(DataTypes.ID) !in ids) return@on

            val m = if ("ultimate_wise" in item.enchants()) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on

            rightClick()
        }.runWhen(detectType.state.map { 1 in it })
    }

    private fun reset() {
        tick = -1
    }
}