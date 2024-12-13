package bufferchanel

import bufferchanel.databuffer.DataForBufferedChannel
import era.utils.files.KiLogger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class BufferedChannel<T: DataForBufferedChannel>(sizeBuffer: Int = 10, dataCreator: (id: Int) -> T) {

    private val datas: Array<InternalData> = Array(sizeBuffer) { i -> InternalData(i, dataCreator) }
    private val mutex = Mutex()

    suspend fun getDataForWrite(): T? { //запись только на свободное место
        mutex.withLock {
            return getInternalDataWithStartStateAndSetWriteState()
        }
    }

    suspend fun getDataForWriteOrRewriteOld(): T? { //запись на свободное место, если нет свободного перезатирает старую
        mutex.withLock {
            getInternalDataWithStartStateAndSetWriteState()?.let { return it }

            getOldInternalDataWithReadyState()?.let {
                it.state = InternalState.WRITE
                return it.cloud
            }

            return null
        }
    }

    suspend fun endWrite(data: T) {
        mutex.withLock{
            findInternalData(data) {
                if(data.validate()) {
                    it.dateWrite = Date().time
                    it.state = InternalState.READY
                } else {
                    it.state = InternalState.START
                    KiLogger.err("Error validate write data in buffered channel")
                }
            }
        }
    }

    suspend fun getFirstDataForRead(): T? { // прочтение старой записи
        mutex.withLock{
            getOldInternalDataWithReadyState()?.let{
                it.state = InternalState.READ
                return it.cloud
            }
            return null
        }
    }

    suspend fun getLastDataForRead(): T? { //прочтение последней записи
        mutex.withLock{
            getLastInternalDataWithReadyState()?.let{
                it.state = InternalState.READ
                return it.cloud
            }
            return null
        }
    }

    suspend fun endRead(data: T) {
        mutex.withLock{
            findInternalData(data) {
                it.state = InternalState.START
            }
        }
    }

    private fun findInternalData(data: T, call: (data: InternalData) -> Unit) {
        for(da in datas) {
            if(da.id == data.getIdData()) {
                call(da)
                return
            }
        }
    }

    private fun getInternalDataWithStartStateAndSetWriteState(): T? {
        for (da in datas) {
            if (da.state == InternalState.START) {
                da.state = InternalState.WRITE
                return da.cloud
            }
        }
        return null
    }

    private fun getOldInternalDataWithReadyState(): InternalData? {
        var id: Int = Int.MAX_VALUE
        var minTime: Long = Long.MAX_VALUE
        for(da in datas) {
            if(da.state == InternalState.READY) {
                if(da.dateWrite < minTime) {
                    id = da.id
                    minTime = da.dateWrite
                }
            }
        }
        for(da in datas) {
            if(da.id == id) {
                return da
            }
        }
        return null
    }

    private fun getLastInternalDataWithReadyState(): InternalData? {
        var id: Int = Int.MAX_VALUE
        var maxTime: Long = Long.MIN_VALUE
        for(da in datas) {
            if(da.state == InternalState.READY) {
                if(da.dateWrite > maxTime) {
                    id = da.id
                    maxTime = da.dateWrite
                }
            }
        }
        for(da in datas) {
            if(da.id == id) {
                return da
            }
        }
        return null
    }

    inner class InternalData(val id: Int, dataCreator: (id: Int) -> T) {
        val cloud: T = dataCreator(id)
        var state: InternalState = InternalState.START
        var dateWrite: Long = Long.MAX_VALUE
    }

    enum class InternalState {
        START, WRITE, READY, READ
    }

}