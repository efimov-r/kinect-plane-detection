package files

import era.utils.files.KiLogger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class KiSerializer<T>(val filename: String) {
    fun writeObject(obj: T) {
        try {
            ObjectOutputStream(FileOutputStream(filename)).use {
                it.writeObject(obj)
            }
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun readObject(): T? {
        try {
            ObjectInputStream(FileInputStream(filename)).use {
                it.readObject()?.let { return it as T }
            }
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }
        return null
    }
 }