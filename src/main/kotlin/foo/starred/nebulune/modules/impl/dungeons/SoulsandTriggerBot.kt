package foo.starred.nebulune.modules.impl.dungeons

import foo.starred.athen.annotations.Load
import foo.starred.athen.annotations.OnlyIn
import foo.starred.athen.api.dungeon.DungeonAPI
import foo.starred.athen.api.location.SkyBlockIsland
import foo.starred.athen.config.Category
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.modules.Module
import foo.starred.nebulune.utils.rightClick
import foo.starred.snowbird.api.client
import foo.starred.snowbird.handlers.Observable.Companion.and
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object SoulsandTriggerBot : Module(
    "Soulsand triggerbot",
    "Triggerbot that automatically places soul sand or chests in P3",
    category = Category.DUNGEONS
) {
    private val set = setOf(Items.SOUL_SAND, Items.CHEST, Items.ENDER_CHEST)

    init {
        on<TickEvent.Client.Start> {
            val a = client.hitResult as? BlockHitResult ?: return@on
            val b = client.level ?: return@on
            val c = client.player?.mainHandItem?.item ?: return@on
            if (c !in set) return@on

            val d = a.blockPos
            if (d.y != 105) return@on

            val e = b.getBlockState(d).block
            if (e != Blocks.STONE_BRICKS) return@on

            val f = b.getBlockState(BlockPos(d.x, d.y + 1, d.z)).block
            if (f != Blocks.LAVA) return@on

            rightClick()
        }.runWhen(DungeonAPI.inBoss and DungeonAPI.floor.map { it?.floorNumber == 7 })
    }
}