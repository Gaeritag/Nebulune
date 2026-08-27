package foo.starred.nebulune.utils

import foo.starred.kommand.IKommand
import foo.starred.kommand.scopes.KommandBuilderScope
import foo.starred.kommand.scopes.KommandCommandScope
import foo.starred.nebulune.Nebulune
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

fun command(block: KommandBuilderScope<FabricClientCommandSource>.() -> Unit) {
    Command.command(Nebulune.modId, block)
}

private object Command : IKommand<FabricClientCommandSource> {
    override val loader: KommandCommandScope<FabricClientCommandSource> = KommandCommandScope()

    init {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            loader.register(dispatcher)
        }
    }
}