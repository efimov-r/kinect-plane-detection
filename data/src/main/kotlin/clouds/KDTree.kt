package clouds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class KDNode(
    val index: Int,
    var left: KDNode? = null,
    var right: KDNode? = null
)

class KDTree {
    private val cloud: Cloud
    private val root: KDNode?

    private constructor(cloud: Cloud, root: KDNode?) {
        this.cloud = cloud
        this.root = root
    }

    companion object {
        fun instanceSingle(cloud: Cloud) : KDTree {
            val indices = IntArray(cloud.size) { it }
            return KDTree(cloud, buildTree(cloud, indices, 0, indices.size, depth = 0))
        }

        suspend fun instanceParallel(cloud: Cloud) : KDTree {
            val indices = IntArray(cloud.size) { it }
            return KDTree(cloud, buildTreeParallel(cloud, indices, 0, indices.size, depth = 0))
        }

        private fun IntArray.selectKth(from: Int, to: Int, k: Int, compare: (Int, Int) -> Int) {
            var left = from
            var right = to - 1
            while (left < right) {
                val pivotIndex = partition(left, right, (left + right) / 2, compare)
                when {
                    k <= pivotIndex -> right = pivotIndex
                    k > pivotIndex -> left = pivotIndex + 1
                }
            }
        }

        private fun IntArray.partition(from: Int, to: Int, pivotIndex: Int, compare: (Int, Int) -> Int): Int {
            val pivotValue = this[pivotIndex]
            swap(pivotIndex, to)
            var storeIndex = from
            for (i in from until to) {
                if (compare(this[i], pivotValue) < 0) {
                    swap(i, storeIndex)
                    storeIndex++
                }
            }
            swap(storeIndex, to)
            return storeIndex
        }

        private fun IntArray.swap(i: Int, j: Int) {
            val tmp = this[i]
            this[i] = this[j]
            this[j] = tmp
        }

        private suspend fun buildTreeParallel(
            cloud: Cloud,
            indices: IntArray,
            from: Int,
            to: Int,
            depth: Int,
            minParallelSize: Int = 10_000
        ): KDNode? {
            if (from >= to) return null

            val axis = depth % 3
            val comparator = when (axis) {
                0 -> { a: Int, b: Int -> cloud.getX(a).compareTo(cloud.getX(b)) }
                1 -> { a: Int, b: Int -> cloud.getY(a).compareTo(cloud.getY(b)) }
                else -> { a: Int, b: Int -> cloud.getZ(a).compareTo(cloud.getZ(b)) }
            }

            val median = (from + to) / 2
            indices.selectKth(from, to, median, comparator)

            val node = KDNode(indices[median])

            if (to - from > minParallelSize) {
                coroutineScope {
                    val leftJob = async(Dispatchers.Default) {
                        buildTreeParallel(cloud, indices, from, median, depth + 1, minParallelSize)
                    }
                    val rightJob = async(Dispatchers.Default) {
                        buildTreeParallel(cloud, indices, median + 1, to, depth + 1, minParallelSize)
                    }
                    node.left = leftJob.await()
                    node.right = rightJob.await()
                }
            } else {
                node.left = buildTreeParallel(cloud, indices, from, median, depth + 1, minParallelSize)
                node.right = buildTreeParallel(cloud, indices, median + 1, to, depth + 1, minParallelSize)
            }

            return node
        }

        /** Построение KD-дерева за O(n log n) */
        private fun buildTree(cloud: Cloud, indices: IntArray, from: Int, to: Int, depth: Int): KDNode? {
            if (from >= to) return null

            val axis = depth % 3
            val comparator = when (axis) {
                0 -> { a: Int, b: Int -> cloud.getX(a).compareTo(cloud.getX(b)) }
                1 -> { a: Int, b: Int -> cloud.getY(a).compareTo(cloud.getY(b)) }
                else -> { a: Int, b: Int -> cloud.getZ(a).compareTo(cloud.getZ(b)) }
            }

            val median = (from + to) / 2
            indices.selectKth(from, to, median, comparator)

            val node = KDNode(indices[median])
            node.left = buildTree(cloud, indices, from, median, depth + 1)
            node.right = buildTree(cloud ,indices, median + 1, to, depth + 1)
            return node
        }
    }


    /** Поиск ближайшего соседа */
    fun findNearestNeighbor(tx: Float, ty: Float, tz: Float): Int? {
        return findNearestNeighbor(root, tx, ty, tz, 0, null, Float.MAX_VALUE)?.index
    }

    private fun findNearestNeighbor(
        node: KDNode?,
        tx: Float, ty: Float, tz: Float,
        depth: Int,
        best: KDNode?,
        bestDistSq: Float
    ): KDNode? {
        if (node == null) return best

        val dx = tx - cloud.getX(node.index)
        val dy = ty - cloud.getY(node.index)
        val dz = tz - cloud.getZ(node.index)
        val distSq = dx * dx + dy * dy + dz * dz

        var newBest = best
        var newBestDistSq = bestDistSq

        if (distSq < newBestDistSq) {
            newBest = node
            newBestDistSq = distSq
        }

        val axis = depth % 3
        val diff = when (axis) {
            0 -> dx
            1 -> dy
            else -> dz
        }

        val (primary, secondary) = if (diff < 0f) node.left to node.right else node.right to node.left

        // Идём в ближайшую ветку
        newBest = findNearestNeighbor(primary, tx, ty, tz, depth + 1, newBest, newBestDistSq)

        // Проверяем вторую ветку
        if (diff * diff < newBestDistSq) {
            newBest = findNearestNeighbor(
                secondary, tx, ty, tz, depth + 1,
                newBest, distanceSquared(tx, ty, tz, newBest!!.index)
            )
        }

        return newBest
    }

    /** Range search */
    fun rangeSearch(
        minX: Float, maxX: Float,
        minY: Float, maxY: Float,
        minZ: Float, maxZ: Float
    ): List<Int> {
        val result = mutableListOf<Int>()
        rangeSearch(root, 0, minX, maxX, minY, maxY, minZ, maxZ, result)
        return result
    }

    private fun rangeSearch(
        node: KDNode?, depth: Int,
        minX: Float, maxX: Float,
        minY: Float, maxY: Float,
        minZ: Float, maxZ: Float,
        result: MutableList<Int>
    ) {
        if (node == null) return

        val x = cloud.getX(node.index)
        val y = cloud.getY(node.index)
        val z = cloud.getZ(node.index)

        if (x in minX..maxX && y in minY..maxY && z in minZ..maxZ) {
            result.add(node.index)
        }

        val axis = depth % 3
        when (axis) {
            0 -> {
                if (minX <= x) rangeSearch(node.left, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
                if (x <= maxX) rangeSearch(node.right, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
            }
            1 -> {
                if (minY <= y) rangeSearch(node.left, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
                if (y <= maxY) rangeSearch(node.right, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
            }
            2 -> {
                if (minZ <= z) rangeSearch(node.left, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
                if (z <= maxZ) rangeSearch(node.right, depth + 1, minX, maxX, minY, maxY, minZ, maxZ, result)
            }
        }
    }

    private fun distanceSquared(tx: Float, ty: Float, tz: Float, idx: Int): Float {
        val dx = tx - cloud.getX(idx)
        val dy = ty - cloud.getY(idx)
        val dz = tz - cloud.getZ(idx)
        return dx * dx + dy * dy + dz * dz
    }

    /** Поиск всех точек в радиусе r от центра (cx, cy, cz), исключая excludeIndex */
    fun radiusSearch(
        cx: Float, cy: Float, cz: Float,
        radius: Float,
        excludeIndex: Int? = null
    ): List<Int> {
        val result = mutableListOf<Int>()
        val radiusSq = radius * radius
        radiusSearch(root, 0, cx, cy, cz, radiusSq, excludeIndex, result)
        return result
    }

    fun radiusSearch(
        radius: Float,
        index: Int
    ): List<Int> {
        val result = mutableListOf<Int>()
        val radiusSq = radius * radius
        radiusSearch(root, 0,
            cloud.getX(index), cloud.getY(index), cloud.getZ(index),
            radiusSq, index, result)
        return result
    }

    private fun radiusSearch(
        node: KDNode?, depth: Int,
        cx: Float, cy: Float, cz: Float,
        radiusSq: Float,
        excludeIndex: Int?,
        result: MutableList<Int>
    ) {
        if (node == null) return

        val idx = node.index
        if (excludeIndex != null && idx == excludeIndex) {
            // пропускаем саму точку
        } else {
            val dx = cx - cloud.getX(idx)
            val dy = cy - cloud.getY(idx)
            val dz = cz - cloud.getZ(idx)
            val distSq = dx * dx + dy * dy + dz * dz
            if (distSq <= radiusSq) {
                result.add(idx)
            }
        }

        val axis = depth % 3
        val diff = when (axis) {
            0 -> cx - cloud.getX(idx)
            1 -> cy - cloud.getY(idx)
            else -> cz - cloud.getZ(idx)
        }

        val diffSq = diff * diff

        // Всегда идем в ту ветку, где центр поиска
        if (diff < 0f) {
            radiusSearch(node.left, depth + 1, cx, cy, cz, radiusSq, excludeIndex, result)
            if (diffSq <= radiusSq) {
                radiusSearch(node.right, depth + 1, cx, cy, cz, radiusSq, excludeIndex, result)
            }
        } else {
            radiusSearch(node.right, depth + 1, cx, cy, cz, radiusSq, excludeIndex, result)
            if (diffSq <= radiusSq) {
                radiusSearch(node.left, depth + 1, cx, cy, cz, radiusSq, excludeIndex, result)
            }
        }
    }
}

