package com.shimao.mybuglylib.core

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.gson.Gson
import com.shimao.mybuglylib.data.HttpClient
import com.shimao.mybuglylib.data.ICallBack
import com.shimao.mybuglylib.data.db.CrashDatabase
import com.shimao.mybuglylib.data.model.ActivityEvent
import com.shimao.mybuglylib.data.model.ClickEvent
import com.shimao.mybuglylib.data.model.FragmentEvent
import com.shimao.mybuglylib.util.BIUtil
import com.shimao.mybuglylib.util.PublicParams
import com.shimao.mybuglylib.util.Util
import java.lang.IllegalArgumentException

/**
 * @author : jian
 * @date   : 2020/7/17 11:53
 * @version: 1.0
 */
class JJBugReport private constructor() {
    companion object{
        private var sInstance : JJBugReport ?= null

        fun getInstance():JJBugReport{
            if(sInstance == null){
                synchronized(JJBugReport::class.java){
                    if (sInstance == null){
                        sInstance = JJBugReport()
                    }
                }
            }
            return sInstance!!
        }

    }
    private var sActivityList = java.util.Collections.synchronizedList(mutableListOf<ActivityEvent>())
    private var sFragmentList = java.util.Collections.synchronizedList(mutableListOf<FragmentEvent>())
    private var sClickList = java.util.Collections.synchronizedList(mutableListOf<ClickEvent>())
    private var sUserMap = mutableMapOf<String,String>()
    private var sUrlList = java.util.Collections.synchronizedList(mutableListOf<String>())
    private var sUrlLimit = 30
    var sBaseUrl:String = ""
        private set
    var sUA:String? = null
        private set
    var sIsDebug:Boolean = false
        private set
    var sContext:Context? = null
        private set
    var sApplication:String? = null
        private set
    var mainActivity:String? = null
        private set

    var sSonPacketName:String = "com.shimao"
        private set

    private var sCallback: JJBugCallBack? = null
    var sDelay: Long = 250

    fun addUserMapInfo(k:String,v:String){
        sUserMap[k] = v
    }

    fun addActivityRecord(activityEvent: ActivityEvent){
        sActivityList.add(activityEvent)
    }

    fun addFragmentRecord(fragmentEvent: FragmentEvent){
        sFragmentList.add(fragmentEvent)
    }

    fun addClickEvent(clickEvent: ClickEvent){
        sClickList.add(clickEvent)
    }

    fun getActivityString():String{
        return Gson().toJson(sActivityList.toTypedArray())
    }

    fun getFragmentString():String{
        return Gson().toJson(sFragmentList.toTypedArray())
    }

    fun getClickString():String{
        return Gson().toJson(sClickList.toTypedArray())
    }

    fun getUrlString():String{
        return Gson().toJson(sUrlList.toTypedArray())
    }

    fun init(context: Context?){
        if (context == null) throw NullPointerException("Context can not be null!")
        if (!Util.isMainProcess(context)){
            return
        }
        if(sBaseUrl.isEmpty()) throw IllegalArgumentException("base url can not be empty!")
        sContext = if (context !is Application) context.applicationContext else context

        if(sApplication==null) {
            sApplication =
                sContext!!.packageManager.getApplicationLabel((sContext as Application).applicationInfo)
                    .toString()
        }
        sIsDebug = sContext!!.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        PublicParams.retrievePublicInfo(sContext!!)
        registerActivityLifecycleCallback()
        JJBugHandler.newInstance(Thread.getDefaultUncaughtExceptionHandler()).setCallback(sCallback).register()
        CrashDatabase.init(context)
        HttpClient.getHttpClient((sContext as Application).applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        Thread(Runnable {
            CrashDatabase.get().crashDao().deleteAlreadyPost()
            val list = CrashDatabase.get().crashDao().getAllUnPostData()
            for (crash in list){
                BIUtil()
                    .setType(BIUtil.TYPE_CRASH)
                    .setCtx(
                        BIUtil.CtxBuilder()
                        .kv("message",crash.message)
                        .kv("exception",crash.exception)
                        .kv("stack",crash.stack)
                        .kv("activitys", crash.activitys)
                        .kv("urls",crash.urls)
                        .kv("fragments",crash.fragments)
                        .kv("clicks",crash.clicks)
                        .build())
                    .execute(object : ICallBack.CallBackImpl<Any>(){
                        override fun onNext(data: Any?) {
                            Log.d("TAGTAG","next")
                            CrashDatabase.get().crashDao().updateStatusById(crash.id)
                        }

                        override fun onError(e: String) {
                            Log.d("TAGTAG","error")
                        }
                    })
            }
        }).start()

    }

    private fun registerActivityLifecycleCallback() {
        (sContext as Application).registerActivityLifecycleCallbacks(JJBugActivityLifecycleCallBack())
    }

    fun callback(callback: JJBugCallBack?): JJBugReport {
        sCallback = callback
        return this
    }

    fun packetName(son:String): JJBugReport{
        sSonPacketName = son
        return this
    }

    fun baseUrl(baseUrl:String): JJBugReport{
        sBaseUrl = baseUrl
        return this
    }

    fun mainActivity(mainActivity:String): JJBugReport{
        this.mainActivity = mainActivity
        return this
    }

    fun applicationName(name: String): JJBugReport {
        sApplication = name
        return this
    }

    fun delay(delay: Long): JJBugReport {
        sDelay = delay
        return this
    }

    fun urlLimit(limit:Int): JJBugReport{
        this.sUrlLimit = limit
        return this
    }

    fun clearRecord(){
        sActivityList.clear()
        sFragmentList.clear()
        sClickList.clear()
    }

    fun addUrlRecord(url:String){
        if(sUrlList.size >= sUrlLimit){
            sUrlList.removeAt(0)
        }
        sUrlList.add(url)
    }
}