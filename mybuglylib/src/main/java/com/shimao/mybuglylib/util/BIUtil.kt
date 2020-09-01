package com.shimao.mybuglylib.util

import com.shimao.mybuglylib.core.JJBugReport
import com.shimao.mybuglylib.data.APIWrapper
import com.shimao.mybuglylib.data.ICallBack
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

/**
 * @author jian
 * @date 2019/9/30 14:56
 * @version 1.0
 */
class BIUtil {

    companion object{
        var lastClickSpm:String? = null
        const val ACTION_CLICK = "click"
        const val START_VIDEO = "startvideo"
        const val STOP_VIDEO = "stopvideo"
        const val ACTION_SHOW = "show"

        const val ACTION_START_APP = "startapp"
        const val ACTION_EXIT_APP = "exitapp"
        const val ACTION_START_APP_NEW = "start_app"
        const val TYPE_PERFORMANCE = "performance"
        const val TYPE_CRASH = "crash"
        const val TYPE_BEHAVIOR = "behavior"
        const val TYPE_CRASH_CAUGHT = "crash_caught"

        /**
         * 将异常信息转化成字符串
         * @param t
         * @return
         * @throws IOException
         */
        fun exception(t:Throwable):String {
            val list = mutableListOf<String>()
            for (stack in t.stackTrace){
                list.add("\"$stack\"")
            }
            return list.toString()
        }
    }


    class CtxBuilder{
        private val params:MutableMap<String,String?> by lazy { LinkedHashMap<String,String?>()}

        fun kv(key:String,value:String?):CtxBuilder{
            if (key.isNotEmpty()){
                params[key] = value
            }
            return this
        }

        fun build():String{
            val builder = StringBuilder()
            builder.append("{")
            if(params.isNotEmpty()){
                for(entry in params.entries) {
                    builder.append("\"").append(entry.key).append("\"")
                    builder.append(":").append("\"").append(entry.value).append("\"")
                    builder.append(",")
                }
                builder.deleteCharAt(builder.lastIndex)
            }
            builder.append("}")
            return builder.toString()
        }


        companion object{

            fun single(key:String,value:String):String{
                return CtxBuilder().kv(key, value).build()
            }
        }
    }

    val TAG = "BILog"
    private var mAction: String = ""
    private var mPage: String = ""
    private var mSpm: String = ""
    private var mCtx: String = ""
    private var mType: String = ""
    var map = mutableMapOf<String,String>()

    fun setAction(action: String): BIUtil {
        mAction = action
        return this
    }

    fun setPage(page:String):BIUtil{
        this.mPage = page
        return this
    }

    fun setType(type:String):BIUtil{
        this.mType = type
        return this
    }

    fun setSpm(spm: String?): BIUtil {
        this.mSpm = spm?:""
        if ("click" == mAction) {
            lastClickSpm = spm
        }
        return this
    }

    fun setCtx(ctx: String?): BIUtil {
        this.mCtx = ctx?:""
        return this
    }

    fun execute(callback:ICallBack<*>?){
        map = PublicParams.getPublicParams()
        setParams()
        map["ctime"] = System.currentTimeMillis().toString()

        APIWrapper.postReport(JJBugReport.getInstance().sBaseUrl,map,callback)
    }

    private fun setParams() {
        mAction.isNotEmpty().let { map["action"] = mAction }
        mPage.isNotEmpty().let { map["page"] = mPage }
        mSpm.isNotEmpty().let { map["spm"] = mSpm }
        mCtx.isNotEmpty().let { map["ctx"] = mCtx }
        mType.isNotEmpty().let { map["type"] = mType }
    }

    override fun toString(): String {
        return "BIUtil(mAction=$mAction, mPage=$mPage, mSpm=$mSpm, mCtx=$mCtx)"
    }
}