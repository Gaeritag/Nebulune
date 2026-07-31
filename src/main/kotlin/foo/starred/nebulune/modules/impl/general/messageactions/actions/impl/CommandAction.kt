@file:Suppress("ConstPropertyName")

package foo.starred.nebulune.modules.impl.general.messageactions.actions.impl

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.general.messageactions.actions.IMessageAction
import foo.starred.athen.modules.impl.general.messageactions.actions.MessageActionType
import foo.starred.snowbird.api.command

@Load
class CommandAction(val command: String) : IMessageAction {
    private val empty = command.isEmpty()

    override val id: Int = int
    override val name: String = str
    override val serializable: String = command

    override fun run() {
        if (empty) return
        command.command()
    }

    companion object {
        const val int = 1
        const val str = "Command"

        init {
            IMessageAction.register(MessageActionType(int, str) { CommandAction(it) })
        }
    }
}