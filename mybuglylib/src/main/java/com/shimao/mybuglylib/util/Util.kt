package com.shimao.mybuglylib.util

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.lang.StringBuilder

/**
 * @author : jian
 * @date   : 2020/7/17 12:05
 * @version: 1.0
 */

object Util {
    fun isMainProcess(context: Context): Boolean {
        try {
            val am = context
                .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfo =
                am.runningAppProcesses
            val mainProcessName = context.packageName
            val myPid = Process.myPid()
            for (info in processInfo) {
                if (info.pid == myPid && mainProcessName == info.processName) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getDefaultUncaughtExceptionHandler(): Thread.UncaughtExceptionHandler? {
        try {
            val clazz: Class<*>
            clazz = if (Build.VERSION.SDK_INT >= 26) {
                Class.forName("com.android.internal.os.RuntimeInit\$KillApplicationHandler")
            } else {
                Class.forName("com.android.internal.os.RuntimeInit\$UncaughtHandler")
            }
            val `object` = clazz.getDeclaredConstructor().newInstance()
            return `object` as Thread.UncaughtExceptionHandler
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    fun isSystemDefaultUncaughtExceptionHandler(handler: Thread.UncaughtExceptionHandler?): Boolean {
        if (handler == null) return false
        val defHandler = getDefaultUncaughtExceptionHandler()
        return defHandler != null && defHandler.javaClass.isInstance(handler)
    }

    /**
     * 将异常信息转化成字符串
     * @param t
     * @return
     * @throws IOException
     */
    fun exception(t:Throwable):String {
        val baos = ByteArrayOutputStream()
        try{
            t.printStackTrace(PrintStream(baos))
        }catch (e: IOException){
            e.printStackTrace()
        }finally{
            baos.close()
        }
        return baos.toString()
    }


}