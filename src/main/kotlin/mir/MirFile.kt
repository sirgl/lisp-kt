package mir

import util.Source

class MirFile(
        val source: Source,
        val functions: List<MirFunction>
)