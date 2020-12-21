package com.shimao.mybuglylib.core

import android.os.Process
import android.util.Log
import com.google.gson.Gson
import com.shimao.mybuglylib.data.ICallBack
import com.shimao.mybuglylib.data.db.CrashDatabase
import com.shimao.mybuglylib.data.db.CrashVO
import com.shimao.mybuglylib.util.BIUtil
import com.shimao.mybuglylib.util.Util
import java.util.*
import kotlin.system.exitProcess

/**
 * @author : jian
 * @date   : 2020/7/17 12:08
 * @version: 1.0
 */
class JJBugHandler private constructor(defHandler: Thread.UncaughtExceptionHandler?) :
    Thread.UncaughtExceptionHandler {
    private val mDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = defHandler
    private var mCallback: JJBugCallBack? = null

    @Synchronized
    override fun uncaughtException(t: Thread, error: Throwable) {
        val stack = BIUtil.exception(error)
        val activitys = JJBugReport.getInstance().getActivityString()
        val fragments = JJBugReport.getInstance().getFragmentString()
        val clicks = JJBugReport.getInstance().getClickString()
        val urls = JJBugReport.getInstance().getUrlString()
        val id = UUID.randomUUID().toString()
        CrashDatabase.get().crashDao().insert(
            CrashVO(
                id = id,
                message = error.message ?: "unknown",
                exception = error::class.java.name,
                stack = stack,
                activitys = activitys,
                fragments = fragments,
                clicks = clicks,
                urls = urls,
                ctime = System.currentTimeMillis(),
                status = 0
            ))
        BIUtil()
            .setType(BIUtil.TYPE_CRASH)
            .setCtx(BIUtil.CtxBuilder()
                .kv("message",error.message)
                .kv("exception",error::class.java.name)
                .kv("stack",stack)
                .kv("activitys", activitys)
                .kv("fragments",fragments)
                .kv("clicks",clicks)
                .kv("urls",urls)
                .build())
            .execute(object :ICallBack.CallBackImpl<Any>(){
                override fun onNext(data: Any?) {
                    Log.d("TAGTAG","next")
                    CrashDatabase.get().crashDao().updateStatusById(id)
                    doAfterPost(t,error)
                }

                override fun onError(e: String) {
                    Log.d("TAGTAG","error")
                    doAfterPost(t,error)
                }
            })
        val time = System.currentTimeMillis()
        while (System.currentTimeMillis() - time < JJBugReport.getInstance().sDelay){

        }
        doAfterPost(t,error)
//        Timer().schedule(object :TimerTask(){
//            override fun run() {
//                Log.d("TAGTAG","timer")
//                doAfterPost(t,error)
//            }
//        },JJBugReport.getInstance().sDelay)

    }

    fun doAfterPost(t: Thread,e:Throwable){
        if (mCallback != null) {
            mCallback!!.throwable(e)
        }
        if (!Util.isSystemDefaultUncaughtExceptionHandler(mDefaultUncaughtExceptionHandler)) {
            if (mDefaultUncaughtExceptionHandler == null) {
                killProcess()
                return
            }
            mDefaultUncaughtExceptionHandler.uncaughtException(t, e)
        } else {
            killProcess()
        }
    }


    fun setCallback(callback: JJBugCallBack?): JJBugHandler {
        mCallback = callback
        return this
    }

    fun register() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    private fun killProcess() {
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    companion object {
        fun newInstance(defHandler: Thread.UncaughtExceptionHandler?): JJBugHandler {
            return JJBugHandler(defHandler)
        }
    }

}
