package linting

interface LintSink {
    fun addLint(lint: Lint)
}

class HasErrorsSink : LintSink {
    var hasErrors = false

    override fun addLint(lint: Lint) {
        if (lint.severity == Severity.Error) {
            hasErrors = true
        }
    }
}

class InterceptingSink(val sink: LintSink, val action: (Lint) -> Unit) : LintSink {
    override fun addLint(lint: Lint) {
        sink.addLint(lint)
        action(lint)
    }
}

class CollectingSink : LintSink {
    val lints = mutableListOf<Lint>()
    override fun addLint(lint: Lint) {
        lints.add(lint)
    }
}

class AppendingSink(val collection: MutableCollection<Lint>) : LintSink {
    override fun addLint(lint: Lint) {
        collection.add(lint)
    }
}

object ThrowingSink : LintSink {
    override fun addLint(lint: Lint) {
        throw LintException(lint)
    }
}

class LintException(val lint: Lint) : Exception()