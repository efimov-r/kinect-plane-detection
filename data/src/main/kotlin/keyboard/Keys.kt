package keyboard

enum class Keys(val chars: List<Char>) {
    K_KEY(listOf('k', 'K', 'л', 'Л')),
    F_KEY(listOf('f', 'F', 'а', 'А')),
    S_KEY(listOf('S', 's', 'Ы', 'ы')),
    P_KEY(listOf('P', 'p', 'З', 'з')),
    ;

    fun isThis(c: Char): Boolean {
        chars.forEach {
            if(it.equals(c)){
                return true
            }
        }
        return false
    }
}