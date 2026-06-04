package com.cocoonpause.tools

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.Parcel
import java.nio.charset.Charset

@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
class ShellExecutor {

    private val binder: IBinder?
    var pServerAvailable: Boolean = false
        private set

    init {
        binder = runCatching {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val getService = serviceManager.getDeclaredMethod("getService", String::class.java)
            val b = getService.invoke(serviceManager, "PServerBinder") as IBinder
            pServerAvailable = true
            b
        }.getOrDefault(null)
    }

    fun executeAsRoot(cmd: String): Result<String?> {
        val b = binder ?: return Result.failure(IllegalStateException("PServer not available"))
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        data.writeStringArray(arrayOf(cmd, "1"))
        runCatching { b.transact(0, data, reply, 0) }
            .getOrElse { return Result.failure(it) }
        val result = reply.createByteArray()?.toString(Charset.defaultCharset())?.trim()?.let {
            if (it == "null") null else it
        }
        data.recycle()
        reply.recycle()
        return Result.success(result)
    }

    private fun getSystemSetting(setting: String): Result<String?> =
        executeAsRoot("settings get system $setting")

    fun getIntSystemSetting(setting: String, defaultValue: Int): Int =
        getSystemSetting(setting)
            .mapCatching { it?.toInt() ?: defaultValue }
            .getOrDefault(defaultValue)

    fun setIntSystemSetting(setting: String, value: Int) {
        executeAsRoot("settings put system $setting $value")
    }
}
