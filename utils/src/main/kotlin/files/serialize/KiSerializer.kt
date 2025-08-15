package files.serialize

import era.utils.files.KiLogger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class KiSerializer<T: Serializable>(val filename: String) {
    fun writeObject(obj: T) {
        try {
            KiLogger.inf(filename)
            ObjectOutputStream(FileOutputStream(filename)).use {
                it.writeObject(obj)
            }
        } catch (ex: Exception) {
            KiLogger.err(ex)
        }

        try {
            ObjectOutputStream(FileOutputStream(filename)).use { outputStream ->
                outputStream.writeObject(obj)
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