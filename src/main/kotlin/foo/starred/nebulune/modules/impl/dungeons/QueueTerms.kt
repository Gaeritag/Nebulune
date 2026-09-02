@file:Suppress("ObjectPropertyName")

package foo.starred.nebulune.modules.impl.dungeons

import foo.starred.athen.annotations.Load
import foo.starred.athen.config.Category
import foo.starred.athen.modules.Module
import foo.starred.athen.modules.impl.dungeon.terminals.solver.data.TerminalClick

@Load
object QueueTerms : Module(
    "Queue terms",
    "Queues terminal clicks to automatically fire.",
    Category.DUNGEONS
) {
    val timeout by config.slider("Resync timeout", 800, 400, 1000, "ms")

    val clicks = mutableListOf<TerminalClick>()
    var yearning = false
}