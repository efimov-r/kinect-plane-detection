package kinect.tarir

class Entity {
    val t: Int
    val min: Int
    val max: Int

    constructor(t: Int, min: Int, max: Int) {
        this.t = t
        this.min = min
        this.max = max
    }

    constructor(t: Int, minmax: Int) {
        this.t = t
        this.min = minmax
        this.max = minmax
    }
}
