package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.collections.plusAssign
import kotlin.math.min

object Threadripper {
    suspend fun CoroutineScope.threadripp(size: Int, countOfThread: Int, call: (start: Int, end: Int) -> Unit) {
        val chunk = (size + countOfThread - 1) / countOfThread
        val jobs = ArrayList<Job>(countOfThread)

        for (t in 0 until countOfThread) {
            val start = t * chunk
            val end = min(size, start + chunk)

            if (start >= end) break

            jobs += launch(Dispatchers.Default) {
                call(start, end)
            }
        }

        jobs.joinAll()
    }
}