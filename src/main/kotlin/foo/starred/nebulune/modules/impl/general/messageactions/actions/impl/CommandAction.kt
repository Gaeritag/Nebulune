@file:Suppress("ConstPropertyName")

package foo.starred.nebulune.modules.impl.general.messageactions.actions.impl

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.general.messageactions.actions.base.IMessageAction
import foo.starred.athen.modules.impl.general.messageactions.actions.data.MessageActionField
import foo.starred.athen.modules.impl.general.messageactions.actions.data.MessageActionType
import foo.starred.snowbird.api.command

@Load
class CommandAction(val command: String) : IMessageAction {
    private val empty = command.isEmpty()

    override val id: Int = int
    override val name: String = str
    override val serializable: Map<String, String> = mapOf("command" to command)

    override fun run() {
        if (empty) return
        command.command()
    }

    override fun resolve(text: String, match: MatchResult?): IMessageAction {
        if (match == null) return this

        var v = this.command
        for (i in match.groupValues.indices.reversed()) {
            v = if (i == 0) text else v.replace("$$i", match.groupValues[i])
        }

        return CommandAction(v)
    }

    companion object {
        const val int = 1
        const val str = "Command"

        init {
            IMessageAction.register(
                MessageActionType(
                    int,
                    str,
                    fields = listOf(MessageActionField("command", "Command", "Command to execute"))
                ) {
                    CommandAction(it["command"] ?: "")
                }
            )
        }
    }
}