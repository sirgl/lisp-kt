package util

import linting.Lint

sealed class ResultWithLints<T>(val lints: List<Lint>) {
    class Ok<T>(val value: T, lints: List<Lint> = emptyList()) : ResultWithLints<T>(lints)
    class Error<T>(lints: List<Lint>) : ResultWithLints<T>(lints)

    fun <R> map(f: (T) -> R): ResultWithLints<R> {
        return when (this) {
            is Ok -> return Ok(f(value), lints)
            is Error -> Error(lints)
        }
    }

    fun mapLintsIfErr(f: (List<Lint>) -> List<Lint>): ResultWithLints<T> {
        return when (this) {
            is Ok -> return Ok(value, lints)
            is Error -> Error(f(lints))
        }
    }

    fun isError(): Boolean {
        return this is Error
    }
}