package kinect.util

import kinect.KinectConstant

class Tari private constructor() {
    private var entities: MutableList<Entity> = ArrayList<Entity>()
    private val tariMas: IntArray

    init {
        initEntities()
        this.tariMas = IntArray(KinectConstant.KINECT_DEPTH_MAX + 1)
        for (i in tariMas.indices) {
            tariMas[i] = calcVal(i)
        }
    }

    private fun getEntity(t: Int): Entity? {
        for (entity in entities) {
            if (entity.t == t) {
                return entity
            }
        }
        return null
    }

    private fun getLastEntity(t: Int): Entity? {
        var t = t
        var res: Entity? = null
        while (t >= KinectConstant.KINECT_DEPTH_MIN) {
            res = getEntity(t)
            if (res != null) {
                return res
            }
            t--
        }
        return null
    }

    private fun getNextEntity(t: Int): Entity? {
        var t = t
        var res: Entity? = null
        while (t <= KinectConstant.KINECT_DEPTH_MAX) {
            res = getEntity(t)
            if (res != null) {
                return res
            }
            t++
        }
        return null
    }

    private fun calcVal(t: Int): Int {
        if (t < KinectConstant.KINECT_DEPTH_MIN) {
            return KinectConstant.KINECT_DEPTH_MM_NULL
        }

        if (t > KinectConstant.KINECT_DEPTH_MAX) {
            return KinectConstant.KINECT_DEPTH_MM_NULL
        }

        val res = getEntity(t)
        if (res != null) {
            return res.max
        }

        val lastEntity = getLastEntity(t)
        val nextEntity = getNextEntity(t)

        if (lastEntity == null || nextEntity == null) {
            return KinectConstant.KINECT_DEPTH_MM_NULL
        }

        val count = nextEntity.t - lastEntity.t
        val mm = nextEntity.max - lastEntity.max
        val v = mm.toDouble() / count

        val v1 = (t - lastEntity.t) * v

        return v1.toInt() + lastEntity.max
    }

    private fun initEntities() {
        entities.add(Entity(KinectConstant.KINECT_DEPTH_MIN, 452))
        entities.add(Entity(365, 465))
        entities.add(Entity(375, 480))
        entities.add(Entity(400, 500))
        entities.add(Entity(410, 505))
        entities.add(Entity(420, 513))
        entities.add(Entity(430, 521))
        entities.add(Entity(440, 530))
        entities.add(Entity(450, 537))
        entities.add(Entity(460, 547))
        entities.add(Entity(470, 556))
        entities.add(Entity(480, 564))
        entities.add(Entity(490, 574))
        entities.add(Entity(500, 583))
        entities.add(Entity(510, 591))
        entities.add(Entity(520, 602))
        entities.add(Entity(530, 615))
        entities.add(Entity(540, 624))
        entities.add(Entity(550, 636))
        entities.add(Entity(560, 647))
        entities.add(Entity(570, 661))
        entities.add(Entity(580, 674))
        entities.add(Entity(590, 688))
        entities.add(Entity(600, 702))
        entities.add(Entity(610, 717))
        entities.add(Entity(620, 731))
        entities.add(Entity(630, 749))
        entities.add(Entity(640, 764))
        entities.add(Entity(650, 782))
        entities.add(Entity(655, 792))
        entities.add(Entity(660, 800))
        entities.add(Entity(665, 811))
        entities.add(Entity(670, 820))
        entities.add(Entity(675, 830))
        entities.add(Entity(680, 840))
        entities.add(Entity(685, 852))
        entities.add(Entity(690, 863))
        entities.add(Entity(695, 873))
        entities.add(Entity(700, 882))
        entities.add(Entity(705, 894))
        entities.add(Entity(710, 909))
        entities.add(Entity(715, 920))
        entities.add(Entity(720, 932))
        entities.add(Entity(725, 945))
        entities.add(Entity(730, 957))
        entities.add(Entity(735, 970))
        entities.add(Entity(740, 981))
        entities.add(Entity(745, 996))
        entities.add(Entity(750, 1010))
        entities.add(Entity(755, 1027))
        entities.add(Entity(760, 1042))
        entities.add(Entity(765, 1060))
        entities.add(Entity(770, 1077))
        entities.add(Entity(775, 1093))
        entities.add(Entity(780, 1112))
        entities.add(Entity(783, 1123))
        entities.add(Entity(786, 1136))
        entities.add(Entity(789, 1145))
        entities.add(Entity(792, 1157))
        entities.add(Entity(795, 1166))
        entities.add(Entity(798, 1182))
        entities.add(Entity(801, 1192))
        entities.add(Entity(804, 1203))
        entities.add(Entity(807, 1215))
        entities.add(Entity(810, 1230))
        entities.add(Entity(813, 1242))
        entities.add(Entity(816, 1253))
        entities.add(Entity(819, 1270))
        entities.add(Entity(822, 1285))
        entities.add(Entity(825, 1297))
        entities.add(Entity(828, 1309))
        entities.add(Entity(831, 1327))
        entities.add(Entity(834, 1343))
        entities.add(Entity(836, 1351))
        entities.add(Entity(838, 1365))
        entities.add(Entity(840, 1375))
        entities.add(Entity(842, 1386))
        entities.add(Entity(844, 1396))
        entities.add(Entity(846, 1406))
        entities.add(Entity(848, 1417))
        entities.add(Entity(850, 1434))
        entities.add(Entity(852, 1442))
        entities.add(Entity(854, 1457))
        entities.add(Entity(856, 1467))
        entities.add(Entity(858, 1481))
        entities.add(Entity(860, 1492))
        entities.add(Entity(862, 1504))
        entities.add(Entity(864, 1520))
        entities.add(Entity(866, 1532))
        entities.add(Entity(868, 1544))
        entities.add(Entity(870, 1559))
        entities.add(Entity(872, 1572))
        entities.add(Entity(874, 1592))
        entities.add(Entity(876, 1602))
        entities.add(Entity(878, 1620))
        entities.add(Entity(880, 1630))
        entities.add(Entity(882, 1646))
        entities.add(Entity(884, 1661))
        entities.add(Entity(886, 1677))
        entities.add(Entity(888, 1695))
        entities.add(Entity(890, 1713))
        entities.add(Entity(892, 1728))
        entities.add(Entity(894, 1749))
        entities.add(Entity(896, 1757))
        entities.add(Entity(898, 1783))
        entities.add(Entity(900, 1792))
        entities.add(Entity(902, 1818))
        entities.add(Entity(904, 1830))
        entities.add(Entity(906, 1858))
        entities.add(Entity(908, 1870))
        entities.add(Entity(910, 1900))
        entities.add(Entity(912, 1914))
        entities.add(Entity(914, 1938))
        entities.add(Entity(916, 1951))
        entities.add(Entity(918, 1980))
        entities.add(Entity(920, 2004))
        entities.add(Entity(922, 2031))
        entities.add(Entity(924, 2050))
        entities.add(Entity(926, 2077))
        entities.add(Entity(928, 2092))
        entities.add(Entity(930, 2128))
        entities.add(Entity(932, 2152))
        entities.add(Entity(934, 2178))
        entities.add(Entity(936, 2202))
        entities.add(Entity(938, 2242))
        entities.add(Entity(940, 2266))
        entities.add(Entity(942, 2302))
        entities.add(Entity(944, 2328))
        entities.add(Entity(946, 2362))
        entities.add(Entity(948, 2382))
        entities.add(Entity(950, 2426))
        entities.add(Entity(952, 2450))
        entities.add(Entity(954, 2497))
        entities.add(Entity(956, 2526))
        entities.add(Entity(958, 2565))
        entities.add(Entity(960, 2595))
        entities.add(Entity(962, 2646))
        entities.add(Entity(964, 2676))
        entities.add(Entity(966, 2736))
        entities.add(Entity(968, 2761))
        entities.add(Entity(970, 2820))
        entities.add(Entity(972, 2873))
        entities.add(Entity(974, 2926))
        entities.add(Entity(976, 2945))
        entities.add(Entity(978, 3015))
        entities.add(Entity(980, 3072))
        entities.add(Entity(982, 3135))
        entities.add(Entity(984, 3210))
        entities.add(Entity(986, 3255))
        entities.add(Entity(988, 3315))
        entities.add(Entity(990, 3390))
        entities.add(Entity(992, 3444))
        entities.add(Entity(994, 3514))
        entities.add(Entity(996, 3592))
        entities.add(Entity(998, 3657))
        entities.add(Entity(1000, 3716))
        entities.add(Entity(1002, 3803))
        entities.add(Entity(1004, 3859))
        entities.add(Entity(1006, 3975))
        entities.add(Entity(1008, 4050))
        entities.add(Entity(1009, 4115))
        entities.add(Entity(1010, 4160))
        entities.add(Entity(1011, 4200))
        entities.add(Entity(1012, 4230))
        entities.add(Entity(1013, 4296))
        entities.add(Entity(1014, 4366))
        entities.add(Entity(1015, 4405))
        entities.add(Entity(1016, 4465))
        entities.add(Entity(1017, 4555))
        entities.add(Entity(1018, 4603))
        entities.add(Entity(1019, 4635))
        entities.add(Entity(1020, 4695))
        entities.add(Entity(1021, 4750))
        entities.add(Entity(1022, 4840))
        entities.add(Entity(1023, 4907))
        entities.add(Entity(1024, 4960))
        entities.add(Entity(1025, 5035))
        entities.add(Entity(1026, 5125))
        entities.add(Entity(1027, 5190))
        entities.add(Entity(1028, 5260))
        entities.add(Entity(1029, 5330))
        entities.add(Entity(1030, 5450))
        entities.add(Entity(1032, 5610))
        entities.add(Entity(1034, 5830))
        entities.add(Entity(1036, 6020))
        entities.add(Entity(1038, 6290))
        entities.add(Entity(KinectConstant.KINECT_DEPTH_MAX, 6595))
    }

    companion object {
        private var instance: Tari? = null

        fun getTari(t: Int): Int {
            if (t > KinectConstant.KINECT_DEPTH_MAX) {
                return KinectConstant.KINECT_DEPTH_MM_NULL
            }
            if (instance == null) {
                instance = Tari()
            }
            return instance!!.tariMas[t]
        }
    }

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
}