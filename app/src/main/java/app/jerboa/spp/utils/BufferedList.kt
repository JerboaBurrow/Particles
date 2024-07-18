package app.jerboa.spp.utils

/**
 * A double buffered mutable list of T
 *
 * Write operations go to the back buffer which is manually committed
 *   to the font buffer.
 *
 * @param T the held type
 * @property sizeFront the size of the front buffer
 * @property sizeBack the size of the back buffer
 * @property indices the indices of the front buffer
 */
class BufferedMutableList<T> {

    private var back: MutableList<T> = mutableListOf()
    private var front: MutableList<T> = mutableListOf()

    var sizeFront: Int = 0
        get() = front.size
        private set

    var sizeBack: Int = 0
        get() = front.size
        private set

    var indices: IntRange = front.indices

    operator fun get(index: Int) = front[index]
    operator fun set(index: Int, element: T) = back.set(index, element)

    fun add(element: T) = back.add(element)
    fun removeAt(index: Int) = back.removeAt(index)
    fun clear() = back.clear()
    fun commit() { front = back }
    fun toList() = front.toList()
}