package foo.starred.nebulune.modules

import com.google.gson.JsonArray
import foo.starred.athen.annotations.impl.LoadableAthenModule
import foo.starred.athen.api.messaging.impl.MessagingAPI.mod
import foo.starred.athen.api.network.http.WebAPI.request
import foo.starred.athen.api.scheduling.Scheduler
import foo.starred.athen.config.theme.impl.catppuccin.MochaColorScheme
import foo.starred.athen.events.LocationEvent
import foo.starred.athen.events.core.on
import foo.starred.nebulune.Nebulune
import foo.starred.kontext.abstraction.client.extension.mainThread
import foo.starred.kontext.abstraction.title.MinecraftTitle
import foo.starred.kontext.api.text.parser.impl.parse
import kotlin.time.Duration.Companion.seconds

@LoadableAthenModule
object UpdateNotifier {
    private const val GITHUB_API = "https://api.github.com/repos/Gaeritag/Nebulune/releases"
    private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)(?:-r(\d+))?""") // https://regex101.com/r/An6dOq/1
    private var times: Int = 0
    private var latestVersion: Version? = null

    private data class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val revision: Int = -1,
        val tag: String
    ) : Comparable<Version> {
        override fun compareTo(other: Version): Int =
            compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch }, { it.revision })

        fun display() = if (revision >= 0) "$major.$minor.$patch-r$revision" else "$major.$minor.$patch"
    }

    init {
        on<LocationEvent.Server.Connect> {
            if (times++ >= 3) return@on
            if (times == 1) {
                Scheduler.schedule(5.seconds) { latest() }
                return@on
            }

            fn()
        }
    }

    private fun fn() {
        val current = Nebulune.modVersion.v() ?: return
        val latest = latestVersion?.takeIf { it > current } ?: return

        mainThread {
            MinecraftTitle.display("<aqua>Update available: <red>${latest.display()}".parse())
            "<hover:<${MochaColorScheme.Mauve.argb}>Click to view release!><click:url:https://github.com/Gaeritag/Nebulune/releases/${latest.tag}><yellow>Update available for <${MochaColorScheme.Green.argb}>Nebulune: <red>${current.display()} <gray>-> <aqua>${latest.display()}".mod()
        }
    }

    private fun String.v(): Version? {
        val match = versionRegex.find(this) ?: return null
        return Version(
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
            match.groupValues[3].toInt(),
            match.groupValues[4].toIntOrNull() ?: -1,
            this
        )
    }

    private fun latest() {
        GITHUB_API.request {
            success<JsonArray> { array ->
                latestVersion = array.mapNotNull { it.asJsonObject["tag_name"]?.asString?.v() }.maxOrNull()
                fn()
            }
        }
    }
}