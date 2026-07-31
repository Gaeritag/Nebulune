
@file:Suppress("ConstPropertyName")

package foo.starred.nebulune.modules.impl.general.messageactions.actions.impl

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.general.messageactions.actions.IMessageAction
import foo.starred.athen.modules.impl.general.messageactions.actions.MessageActionType
import foo.starred.snowbird.api.message

@Load
class MessageAction(val message: String) : IMessageAction {
    private val empty = message.isEmpty()

    override val id: Int = int
    override val name: String = str
    override val serializable: String = message

    override fun run() {
        if (empty) return
        message.message()
    }

    companion object {
        const val int = 2
        const val str = "Message"

        init {
            IMessageAction.register(MessageActionType(int, str) { MessageAction(it) })
        }
    }
}