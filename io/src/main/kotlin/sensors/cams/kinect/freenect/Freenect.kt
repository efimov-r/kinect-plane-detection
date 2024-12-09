package sensors.cams.kinect.freenect

import com.sun.jna.*
import com.sun.jna.ptr.PointerByReference
import era.utils.files.KiLogger
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Freenect : Library {
    init {
        try {
            NativeLibrary.addSearchPath("freenect", "/usr/local/lib")
            val instance = NativeLibrary.getInstance("freenect")
            System.err.println("Loaded " + instance.getName() + " from " + instance.getFile().getCanonicalPath())
            Native.register(instance)
        } catch (e: IOException) {
            throw AssertionError(e)
        }
    }

    fun createContext(): Context {
        val ctxPtr = PointerByReference()
        val rval = freenect_init(ctxPtr, Pointer.NULL)
        if (rval == 0) {
            val ctx = NativeContext(ctxPtr.getValue())
            ctx.startEventThread()
            return ctx
        }
        throw IllegalStateException("init() returned " + rval)
    }

    private external fun freenect_init(ctx: PointerByReference?, usb_ctx: Pointer?): Int

    private external fun freenect_shutdown(ctx: Freenect.NativeContext?): Int

    private external fun freenect_set_log_level(ctx: Freenect.NativeContext?, level: Int)

    private external fun freenect_set_log_callback(ctx: Freenect.NativeContext?, cb: Freenect.NativeLogCallback?)

    private external fun freenect_process_events(ctx: Freenect.NativeContext?): Int

    private external fun freenect_num_devices(ctx: Freenect.NativeContext?): Int

    private external fun freenect_open_device(ctx: Freenect.NativeContext?, dev: PointerByReference?, index: Int): Int

    private external fun freenect_close_device(dev: Freenect.NativeDevice?): Int

    private external fun freenect_set_user(dev: Freenect.NativeDevice?, user: Pointer?)

    private external fun freenect_get_user(dev: Freenect.NativeDevice?): Pointer?

    private external fun freenect_set_depth_callback(dev: Freenect.NativeDevice?, cb: Freenect.NativeDepthCallback?)

    private external fun freenect_set_video_callback(dev: Freenect.NativeDevice?, cb: Freenect.NativeVideoCallback?)

    private external fun freenect_set_depth_buffer(dev: Freenect.NativeDevice?, buf: ByteBuffer?): Int

    private external fun freenect_set_video_buffer(dev: Freenect.NativeDevice?, buf: ByteBuffer?): Int

    private external fun freenect_start_depth(dev: Freenect.NativeDevice?): Int

    private external fun freenect_start_video(dev: Freenect.NativeDevice?): Int

    private external fun freenect_stop_depth(dev: Freenect.NativeDevice?): Int

    private external fun freenect_stop_video(dev: Freenect.NativeDevice?): Int

    private external fun freenect_select_subdevices(ctx: Freenect.NativeContext?, flag: Int): Int

    private external fun freenect_get_video_mode_count(): Int

    private external fun freenect_get_video_mode(mode_num: Int): FrameMode.ByValue?

    private external fun freenect_get_current_video_mode(dev: NativeDevice?): FrameMode.ByValue?

    private external fun freenect_find_video_mode(res: Int, fmt: Int): FrameMode.ByValue

    private external fun freenect_set_video_mode(dev: NativeDevice?, mode: FrameMode.ByValue?): Int

    private external fun freenect_get_depth_mode_count(): Int

    private external fun freenect_get_depth_mode(mode_num: Int): FrameMode.ByValue?

    private external fun freenect_get_current_depth_mode(dev: NativeDevice?): FrameMode.ByValue?

    private external fun freenect_find_depth_mode(res: Int, fmt: Int): FrameMode.ByValue

    private external fun freenect_set_depth_mode(dev: NativeDevice?, mode: FrameMode.ByValue?): Int

    class NativeContext : PointerType, Context {
        private var eventThread: EventThread? = null

        constructor()

        class LogCallback : NativeLogCallback {
            override fun callback(dev: NativeDevice?, logLevel: Int, msg: String?
            ) = KiLogger.devInfo(dev?.deviceIndex.toString(), msg ?: "empty mess")
        }

        constructor(ptr: Pointer?) : super(ptr)

        override fun setLogLevel(level: LogLevel) {
            freenect_set_log_level(this, level.intValue())
        }

        override fun numDevices(): Int {
            return freenect_num_devices(this)
        }

        override fun openDevice(index: Int): Device {
            val devicePtr = PointerByReference()
            val rval =
                freenect_open_device(this, devicePtr, index)
            check(rval == 0) { "freenect_open_device() returned " + rval }
            freenect_set_log_callback(
                this,
                logInst()
            )
            return NativeDevice(devicePtr.getValue(), index)
        }

        protected fun processEvents() {
            freenect_process_events(this)
        }

        fun startEventThread() {
            if (eventThread == null || !eventThread!!.isAlive()) {
                eventThread = EventThread(this)
                eventThread!!.start()
            }
        }

        protected fun stopEventThread() {
            if (eventThread != null) {
                eventThread!!.kill()
                eventThread = null
            }
        }

        override fun shutdown() {
            stopEventThread()
            freenect_shutdown(this)
        }

        override fun selectSubdev() {
            freenect_select_subdevices(this, 2) //camera only
        }

        companion object {
            private var logInstance : NativeContext.LogCallback? = null

            fun logInst() : NativeContext.LogCallback {
                if(logInstance == null) {
                    logInstance =
                        NativeContext.LogCallback()
                }
                return logInstance!!
            }
        }
    }

    class NativeDevice : PointerType, Device {
        override var videoMode: FrameMode? = null
        private var videoBuffer: ByteBuffer? = null

        override var depthMode: FrameMode? = null
        private var depthBuffer: ByteBuffer? = null

        private var index = 0

        class VideoCallback(val videoHandler: VideoHandler?, val videoMode: FrameMode?, val videoBuffer: ByteBuffer?) :
            NativeVideoCallback {
            override fun callback(dev: Pointer?, frame: Pointer?, timestamp: Int) {
                videoHandler!!.onFrameReceived(videoMode, videoBuffer, timestamp)
            }
        }

        class DepthCallback(val depthHandler: DepthHandler?, val depthMode: FrameMode?, val depthBuffer: ByteBuffer?) :
            NativeDepthCallback {
            override fun callback(dev: Pointer?, depth: Pointer?, timestamp: Int) {
                    depthHandler?.onFrameReceived(depthMode, depthBuffer, timestamp)
            }
        }

        constructor()

        constructor(ptr: Pointer?, index: Int) : super(ptr) {
            this.index = index
        }

        override fun close() {
            freenect_close_device(this)
        }

        override val deviceIndex: Int
            get() = index

        override fun setDepthFormat(fmt: DepthFormat?) {
            setDepthFormat(fmt, Resolution.MEDIUM)
        }

        override fun setVideoFormat(fmt: VideoFormat?) {
            setVideoFormat(fmt, Resolution.MEDIUM)
        }

        override fun setDepthFormat(fmt: DepthFormat?, res: Resolution?) {
            val mode = freenect_find_depth_mode(
                res!!.intValue(),
                fmt!!.intValue()
            )
            if (mode.isValid()) {
                freenect_set_depth_mode(this, mode)
                depthBuffer = ByteBuffer.allocateDirect(mode.frameSize)
                depthBuffer!!.order(ByteOrder.nativeOrder())
                freenect_set_depth_buffer(this, depthBuffer)
                this.depthMode = mode
            }
        }

        override fun setVideoFormat(fmt: VideoFormat?, res: Resolution?) {
            val mode = freenect_find_video_mode(
                res!!.intValue(),
                fmt!!.intValue()
            )
            if (mode.isValid()) {
                freenect_set_video_mode(this, mode)
                videoBuffer = ByteBuffer.allocateDirect(mode.frameSize)
                videoBuffer!!.order(ByteOrder.nativeOrder())
                freenect_set_video_buffer(this, videoBuffer)
                this.videoMode = mode
            }
        }

        override fun startVideo(handler: VideoHandler?): Int {
            freenect_set_video_callback(
                this,
                videoInst(
                    handler,
                    videoMode,
                    videoBuffer
                )
            )
            return freenect_start_video(this)
        }

        override fun stopVideo(): Int {
            val rval = freenect_stop_video(this)
            freenect_set_video_callback(this, null)
            return rval
        }

        override fun startDepth(handler: DepthHandler?): Int {
            freenect_set_depth_callback(
                this,
                depthInst(
                    handler,
                    depthMode,
                    depthBuffer
                )
            )
            return freenect_start_depth(this)
        }

        override fun stopDepth(): Int {
            val rval = freenect_stop_depth(this)
            freenect_set_depth_callback(this, null)
            return rval
        }

        companion object {
            private var depthInstance: DepthCallback? = null
            private var videoInstance: VideoCallback? = null

            fun depthInst(depthHandler: DepthHandler?, depthMode: FrameMode?, depthBuffer: ByteBuffer?) : DepthCallback {
                if(depthInstance == null) {
                    depthInstance =
                        DepthCallback(
                            depthHandler,
                            depthMode,
                            depthBuffer
                        )
                }
                return depthInstance!!
            }

            fun videoInst(videoHandler: VideoHandler?, videoMode: FrameMode?, videoBuffer: ByteBuffer?) : VideoCallback {
                if(videoInstance == null) {
                    videoInstance =
                        VideoCallback(
                            videoHandler,
                            videoMode,
                            videoBuffer
                        )
                }
                return videoInstance!!
            }
        }
    }

    private class EventThread(private val ctx: NativeContext?) : Thread() {
        @Volatile
        private var alive = true

        init {
            setDaemon(true)
            setName("FreenectEventThread")
        }

        fun kill() {
            this.alive = false
        }

        override fun run() {
            while (alive) {
                freenect_process_events(ctx)
            }
        }
    }

    class TiltState : PointerType()

    // function prototypes from libfreenect.h
    // These must match the names used in the library!
    interface NativeLogCallback : Callback {
        fun callback(dev: NativeDevice?, logLevel: Int, msg: String?)
    }

    private interface NativeDepthCallback : Callback {
        fun callback(dev: Pointer?, depth: Pointer?, timestamp: Int)
    }

    private interface NativeVideoCallback : Callback {
        fun callback(dev: Pointer?, frame: Pointer?, timestamp: Int)
    }
}