package query

import org.junit.jupiter.api.Test


class QuerySubsystemTest {
    @Test
    fun `test plain`() {
        val database : Database = DatabaseImpl(listOf(
                SimpleValue(aDescriptor, A(12)),
                SimpleValue(dDescriptor, D(5))
        ))
        database.registerQuery(BQuery())
        database.registerQuery(CQuery())
        @Suppress("USELESS_CAST")
        assert((database.queryFor(cDescriptor) as Any) is C)
    }
}

private class A(value: Int)
private class D(value: Int)

private class B(val a: A)

private class C(val b: B)

private val aDescriptor = SingleValueDescriptor<A>("A")
private val dDescriptor = SingleValueDescriptor<D>("D")
private val bDescriptor = SingleValueDescriptor<B>("B")
private val cDescriptor = SingleValueDescriptor<C>("C")

private class AD(val a: A, val d: D)

private class BQuery : Query<AD, B> {
    override fun doQuery(input: AD): B {
        return B(input.a)
    }

    private val adDescriptor = MultiValueDescriptor(listOf("A", "D")) { AD(it[0] as A, it[1] as D) }
    override val inputDescriptor: ValueDescriptor<AD>
        get() = adDescriptor
    override val outputDescriptor: SingleValueDescriptor<B>
        get() = bDescriptor
}


private class CQuery : Query<B, C> {
    override fun doQuery(input: B): C {
        return C(input)
    }

    override val inputDescriptor: ValueDescriptor<B>
        get() = bDescriptor
    override val outputDescriptor: SingleValueDescriptor<C>
        get() = cDescriptor
}