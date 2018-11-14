package mir.opt

import mir.MirFunction

interface MirPhase {
    val name: String

    fun apply(function: MirFunction)
}