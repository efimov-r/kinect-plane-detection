package vectors

import kotlin.math.abs

// проекция точек на (u,v) + оригинальные 3D координаты
data class P2D(val x: Float,
               val y: Float,
               val original: Int) {
    inline fun cross(a: P2D, b: P2D): Float =
        (b.x - a.x) * (y - a.y) - (b.y - a.y) * (x - a.x)

    inline fun distanceFromLine(a: P2D, b: P2D): Float =
        abs((b.x - a.x) * (a.y - y) - (a.x - x) * (b.y - a.y))

    inline fun crossOld(o: P2D, a: P2D): Float {
        return (a.x - o.x) * (y - o.y) - (a.y - o.y) * (x - o.x)
    }
}
