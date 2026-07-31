package foo.starred.nebulune.modules.impl.dungeons

import foo.starred.athen.annotations.Load
import foo.starred.athen.api.dungeon.terminals.TerminalAPI
import foo.starred.athen.api.dungeon.terminals.TerminalType
import foo.starred.athen.config.Category
import foo.starred.athen.events.DungeonEvent
import foo.starred.athen.events.TickEvent
import foo.starred.athen.events.core.runWhen
import foo.starred.athen.modules.Module
import foo.starred.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import foo.starred.athen.modules.impl.dungeon.terminals.solver.base.Click
import foo.starred.athen.modules.impl.dungeon.terminals.solver.impl.*
import foo.starred.nebulune.accessors.ITerminalAccessor
import foo.starred.snowbird.api.client
import kotlin.random.Random

@Load
object HoverTerms : Module(
    "Hover terms",
    "Clicks terminals based on where your cursor is hovering :eyes:",
    Category.DUNGEONS
) {
    private val minDelay by config.slider("Min delay", 50, 0, 400, "ms")
    private val maxDelay by config.slider("Max delay", 120, 0, 400, "ms")

    private val solvers = mapOf(
        TerminalType.NUMBERS to NumbersSolver,
        TerminalType.PANES to PanesSolver,
        TerminalType.NAME to NameSolver,
        TerminalType.COLORS to ColorsSolver,
        TerminalType.RUBIX to RubixSolver,
    )

    private var slot0 = -1
    private var time = 0L

    init {
        on<TickEvent.Client.Start> {
            val type = TerminalAPI.terminal ?: return@on
            if (type == TerminalType.MELODY) return@on

            val now = System.currentTimeMillis()
            if (now - TerminalAPI.open < TerminalSolver.fcDelay) return@on
            val solver = solvers[type] as? ITerminalAccessor ?: return@on

            val uiScale = 3f * TerminalSolver.`ui$scale`
            val mx = client.mouseHandler.xpos().toFloat() / uiScale
            val my = client.mouseHandler.ypos().toFloat() / uiScale

            val int0 = solver.`nebulune$int0`()
            val int1 = solver.`nebulune$int1`()

            val sp = solver.`nebulune$float`()
            val pad = TerminalSolver.`ui$padding`
            val slots = type.slots
            val gridW = int0 * sp + 2 * pad
            val gridH = (slots / 9 - 2) * sp + 2 * pad
            val headerH = if (TerminalSolver.`ui$hideHeader`) 0f else 20f
            val padding = if (TerminalSolver.`ui$hideHeader`) 0f else 6f

            val ox = client.window.width / uiScale / 2 - gridW / 2
            val oy = client.window.height / uiScale / 2 - (gridH + headerH + padding) / 2

            val x = ((mx - ox - pad) / sp).toInt() + int1
            val y = ((my - (oy + headerH + padding) - pad) / sp).toInt() + 1
            if (x !in int1 until int1 + int0 || y < 1) return@on

            val slot = x + y * 9
            if (slot >= slots) return@on

            val list = solver.`nebulune$getList`() ?: return@on
            val c = list.find { it.slot == slot } ?: return@on reset()

            if (slot != slot0) {
                slot0 = slot
                time = now + delay()
            }

            if (now < time) return@on

            slot0 = -1
            val final = if (type == TerminalType.RUBIX) Click(c.slot, if (c.button > 0) 0 else 1) else c
            type.impl.click(final.slot, final.button)
        }.runWhen(TerminalAPI.opened)

        on<DungeonEvent.Terminal.Open> {
            reset()
        }
    }

    fun reset() {
        slot0 = -1
        time = 0L
    }

    private fun delay(): Long {
        val lo = minDelay.toLong()
        val hi = maxDelay.toLong()
        return if (lo >= hi) lo else Random.nextLong(lo, hi + 1)
    }
}