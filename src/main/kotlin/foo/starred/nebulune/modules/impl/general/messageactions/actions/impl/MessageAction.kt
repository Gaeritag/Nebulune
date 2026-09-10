@file:Suppress("ConstPropertyName")

package foo.starred.nebulune.modules.impl.general.messageactions.actions.impl

import foo.starred.athen.annotations.Load
import foo.starred.athen.modules.impl.general.messageactions.actions.base.IMessageAction
import foo.starred.athen.modules.impl.general.messageactions.actions.data.MessageActionField
import foo.starred.athen.modules.impl.general.messageactions.actions.data.MessageActionType
import foo.starred.snowbird.api.message

@Load
class MessageAction(val message: String) : IMessageAction {
    private val empty = message.isEmpty()

    override val id: Int = int
    override val name: String = str
    override val serializable: Map<String, String> = mapOf("message" to message)

    override fun run() {
        if (empty) return
        message.message()
    }

    override fun resolve(text: String, match: MatchResult?): IMessageAction {
        if (match == null) return this

        var v = this.message
        for (i in match.groupValues.indices.reversed()) {
            v = if (i == 0) text else v.replace("$$i", match.groupValues[i])
        }

        return MessageAction(v)
    }

    companion object {
        const val int = 2
        const val str = "Message"

        init {
            IMessageAction.register(
                MessageActionType(
                    int,
                    str,
                    fields = listOf(MessageActionField("message", "Message", "Message to send"))
                ) {
                    MessageAction(it["message"] ?: "")
                }
            )
        }
    }
}