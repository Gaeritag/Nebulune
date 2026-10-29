@file:Suppress("Unused")

package foo.starred.nebulune.modules.impl.slayer

import foo.starred.athen.annotations.impl.AthenModuleScope
import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.api.location.island.impl.PresetSkyBlockIsland
import foo.starred.athen.api.slayers.SlayerAPI
import foo.starred.athen.api.slayers.enums.type.impl.SlayerBoss
import foo.starred.athen.config.dsl.impl.category.ConfigCategory
import foo.starred.athen.events.PlayerEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.modules.Module
import foo.starred.kbus.extensions.runWhen
import foo.starred.kontext.abstraction.client.extension.client
import foo.starred.kontext.api.item.data.enchant.SkyBlockItemEnchant
import foo.starred.kontext.api.item.extension.get
import foo.starred.kontext.api.item.resolver.impl.SkyBlockEnchantKontextItemDataResolver
import foo.starred.nebulune.utils.rightClick
import net.minecraft.world.phys.EntityHitResult
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.profile.StatsAPI

@LoadableAthenModule
@AthenModuleScope(islands = [PresetSkyBlockIsland.THE_END])
object AutoSoulcry : Module(
    "Auto soulcry",
    "Automatically uses the soulcry ability of your katana!",
    ConfigCategory.SLAYER
) {
    private val mana by config.switch("Check mana", true)
    private val hitbox by config.switch("Check boss hitbox", true)
    private val minDelay by config.slider("Min delay", 1, 0, 5, "ticks")
    private val maxDelay by config.slider("Max delay", 3, 0, 5, "ticks")
    private val detectType = config.multiSelector("Detection type", listOf("Tick based", "Attack based")).unique("detectType")
    private val otherBosses by config.switch("Work on other's bosses")
    private val _unused0 by config.information("The option <red>\"Work on other's bosses\"<r> requires you to have <red>Attack based<r> selected in Detection type!")

    private val ids = setOf("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA")
    private var tick = -1

    init {
        on<TickEvent.Client.Start> {
            val slayer = SlayerAPI.slayer
            if (slayer?.type as? SlayerBoss != SlayerBoss.Voidgloom) return@on reset()
            //~ if >= 26.2 'client.screen' -> 'client.gui.screen()'
            if (client.screen != null) return@on reset()

            val player = client.player ?: return@on reset()
            val item = player.mainHandItem

            if (item.getData(DataTypes.ID) !in ids) return@on reset()
            if (hitbox && (client.hitResult as? EntityHitResult)?.entity != slayer.entity) return@on
            if (player.cooldowns.isOnCooldown(item)) return@on

            val enchants = item.get<SkyBlockEnchantKontextItemDataResolver, List<SkyBlockItemEnchant>>()
            val m = if (enchants?.any { it.id == "ultimate_wise" } == true) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on reset()

            if (tick == -1) return@on ::tick.set((minDelay..maxDelay.coerceAtLeast(minDelay)).random())
            if (tick-- > 0) return@on

            rightClick()
            reset()
        }.runWhen(detectType.state.map { 0 in it })

        on<PlayerEvent.Attack.Entity> {
            val si = SlayerAPI.bosses[entity] ?: return@on
            if (!si.owned && !otherBosses) return@on

            val player = client.player ?: return@on reset()
            val item = player.mainHandItem

            if (item.getData(DataTypes.ID) !in ids) return@on
            if (player.cooldowns.isOnCooldown(item)) return@on

            val enchants = item.get<SkyBlockEnchantKontextItemDataResolver, List<SkyBlockItemEnchant>>()
            val m = if (enchants?.any { it.id == "ultimate_wise" } == true) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on

            rightClick()
        }.runWhen(detectType.state.map { 1 in it })
    }

    private fun reset() {
        tick = -1
    }
}