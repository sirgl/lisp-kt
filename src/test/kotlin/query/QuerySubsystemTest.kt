package query

import org.junit.jupiter.api.Test


class QuerySubsystemTest {
    @Test
    fun `test plain`() {
        val database : Database = DatabaseImpl(listOf(
                DatabaseValue(aDescriptor, A(12)),
            DatabaseValue(dDescriptor, D(5))
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

private val aDescriptor = TypedKey<A>("A")
private val dDescriptor = TypedKey<D>("D")
private val bDescriptor = TypedKey<B>("B")
private val cDescriptor = TypedKey<C>("C")

private class AD(val a: A, val d: D)

private class BQuery : Query<B> {
    override fun doQuery(input: TypedStorage): B {
        return B(input[aDescriptor])
    }

    private val adDescriptor = MultiKey(listOf(aDescriptor, dDescriptor))
    override val inputKey = adDescriptor
    override val outputDescriptor = bDescriptor
}


private class CQuery : SimpleQuery<B, C>("") {
    override fun doQuery(input: B): C {
        return C(input)
    }

    override val inputKey= bDescriptor
    override val outputDescriptor = cDescriptor
}