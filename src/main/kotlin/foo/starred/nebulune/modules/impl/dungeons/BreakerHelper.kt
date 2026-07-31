package foo.starred.nebulune.modules.impl.dungeons

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.dungeon.DungeonAPI
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.PacketEvent
import foo.starred.athen.events.PlayerEvent
import foo.starred.athen.modules.Module
import foo.starred.snowbird.api.client
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findThenNull

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object BreakerHelper : Module(
    "Breaker helper",
    "Utilities for the Dungeon Breaker.",
    Category.DUNGEONS
) {
    private val preventSecrets by config.switch("Prevent mining secrets")
    private val zeroPing by config.switch("Instamine during fatigue")

    private val chargeRegex = Regex("Charges: (?<cur>\\d+)/(?<max>\\d+)⸕")

    private var charges: Int = 0
    private var max: Int = 0

    init {
        on<PlayerEvent.Attack.Block> {
            if (!zeroPing && !preventSecrets) return@on

            val p = client.player ?: return@on
            val l = client.level ?: return@on
            val i = p.mainHandItem.takeIf { it != ItemStack.EMPTY } ?: return@on

            if (i.getSkyBlockId()?.skyblockId != "DUNGEONBREAKER") return@on
            val a = l.getBlockState(pos).block in set

            if (preventSecrets && a) return@on cancel()
            if (a) return@on

            if (!zeroPing) return@on
            if (charges == 0) return@on
            if (DungeonAPI.inBoss.value && DungeonAPI.floor.value?.floorNumber != 7) return@on
            if (!p.hasEffect(MobEffects.MINING_FATIGUE)) return@on

            l.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
        }

        on<PacketEvent.Receive, ClientboundContainerSetSlotPacket> {
            if (item.getSkyBlockId()?.skyblockId != "DUNGEONBREAKER") return@on
            val lore = item.getRawLore()

            for (l in lore) {
                chargeRegex.findThenNull(l, "cur", "max") { (i1, i2) ->
                    charges = i1.toInt()
                    max = i2.toInt()
                } ?: break
            }
        }

        on<LocationEvent.Server.Connect> {
            charges = 0
            max = 0
        }
    }

    private val set = setOf(
        Blocks.BARRIER,
        Blocks.BEDROCK,
        Blocks.COMMAND_BLOCK,
        Blocks.CHAIN_COMMAND_BLOCK,
        Blocks.REPEATING_COMMAND_BLOCK,
        Blocks.PLAYER_HEAD,
        Blocks.PLAYER_WALL_HEAD,
        Blocks.SKELETON_SKULL,
        Blocks.SKELETON_WALL_SKULL,
        Blocks.WITHER_SKELETON_SKULL,
        Blocks.WITHER_SKELETON_WALL_SKULL,
        Blocks.TNT,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.END_PORTAL_FRAME,
        Blocks.END_PORTAL,
        Blocks.PISTON,
        Blocks.PISTON_HEAD,
        Blocks.STICKY_PISTON,
        Blocks.MOVING_PISTON,
        Blocks.LEVER,
        Blocks.STONE_BUTTON
    )
}