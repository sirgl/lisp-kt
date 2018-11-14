package query

import org.junit.jupiter.api.Test


class QuerySubsystemTest {
    @Test
    fun `test plain`() {
        val database : Database = DatabaseImpl(listOf(
                SimpleValue(aDescriptor, A(12))
        ))
        database.registerQuery(BQuery())
        database.registerQuery(CQuery())
        @Suppress("USELESS_CAST")
        assert((database.queryFor(cDescriptor) as Any) is C)
    }
}

class A(value: Int)

class B(val a: A)

class C(val b: B)

val aDescriptor = SingleValueDescriptor<A>("A")
val bDescriptor = SingleValueDescriptor<B>("B")
val cDescriptor = SingleValueDescriptor<C>("C")

class BQuery : Query<A, B> {
    override fun doQuery(input: A): B {
        return B(input)
    }

    override val inputDescriptor: ValueDescriptor<A>
        get() = aDescriptor
    override val outputDescriptor: SingleValueDescriptor<B>
        get() = bDescriptor
}


class CQuery : Query<B, C> {
    override fun doQuery(input: B): C {
        return C(input)
    }

    override val inputDescriptor: ValueDescriptor<B>
        get() = bDescriptor
    override val outputDescriptor: SingleValueDescriptor<C>
        get() = cDescriptor
}