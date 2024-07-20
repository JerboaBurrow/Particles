package app.jerboa.spp

import app.jerboa.spp.utils.BufferedMutableList
import junit.framework.TestCase.assertEquals
import org.junit.Test

class BufferedMutableListTest {

    @Test
    fun empty_list(){
        val list = BufferedMutableList<Int>()
        assertEquals(list.sizeBack, list.sizeFront)
        assertEquals(list.sizeBack, 0)
        assert(list.indices.isEmpty())
        assert(list.toList().isEmpty())
    }

    @Test
    fun adding(){
        val list = BufferedMutableList<Int>()
        list.add(1)
        assertEquals(1, list.sizeBack)
        assertEquals(0, list.sizeFront)
        assert(list.indices.isEmpty())
        assert(list.toList().isEmpty())

        list.commit()
        assertEquals(1, list.sizeBack)
        assertEquals(1, list.sizeFront)
        assertEquals(0..0, list.indices)
        assertEquals(listOf<Int>(1), list.toList())

        assertEquals(1, list[0])
        list[0] = 2
        assertEquals(1, list[0])
        list.commit()
        assertEquals(2, list[0])

        list.clear()
        assertEquals(list.sizeBack, 0)
        assertEquals(1, list.sizeFront)
        assertEquals(0..0, list.indices)
        assertEquals(listOf<Int>(2), list.toList())
        list.commit()
        assertEquals(list.sizeBack, list.sizeFront)
        assertEquals(list.sizeBack, 0)
        assert(list.indices.isEmpty())
        assert(list.toList().isEmpty())
    }

    @Test
    fun get_set(){
        val list = BufferedMutableList<Int>()
        list.add(1)
        list.commit()

        assertEquals(1, list[0])
        list[0] = 2
        assertEquals(1, list[0])
        list.commit()
        assertEquals(2, list[0])
    }

    @Test
    fun clear(){
        val list = BufferedMutableList<Int>()
        list.add(1)
        list.commit()

        list.clear()
        assertEquals(list.sizeBack, 0)
        assertEquals(1, list.sizeFront)
        assertEquals(0..0, list.indices)
        assertEquals(listOf<Int>(1), list.toList())
        list.commit()
        assertEquals(list.sizeBack, list.sizeFront)
        assertEquals(list.sizeBack, 0)
        assert(list.indices.isEmpty())
        assert(list.toList().isEmpty())
    }

}