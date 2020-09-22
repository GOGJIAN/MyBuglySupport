package com.shimao.mybuglylib.data

import android.text.TextUtils
import android.util.Log
import com.shimao.mybuglylib.core.JJBugReport
import okhttp3.*
import java.io.IOException
import java.lang.Exception


/**
 * @author : jian
 * @date   : 2020/7/17 14:27
 * @version: 1.0
 */
class APIWrapper {
    companion object{
        fun postReport(url: String,map: MutableMap<String,String>, callback: ICallBack<*>?){
            val client = HttpClient.getHttpClient(true)
            val call = client.newCall(createOkHttpRequest(url, map))
            var response:Response? = null
            Thread {
                try {
                    response = call.execute()
                    if (response!!.isSuccessful){
                        callback?.onNext(null)
                    }
                }catch (e: Exception){
                    callback?.onError(e.toString())
                }finally {
                    response?.close()
                }
            }.start()
        }

        private fun createOkHttpRequest(
            url: String, map: MutableMap<String,String>
        ): Request {

            val requestBuilder: Request.Builder = Request.Builder()

            requestBuilder.url(url)
            val formBodyBuilder = FormBody.Builder()
            for (key in map.keys) {
                formBodyBuilder.add(key, map[key]!!)
            }
            val requestBody: RequestBody = formBodyBuilder.build()
            requestBuilder.method("POST", requestBody)

            requestBuilder.header("User-Agent", JJBugReport.getInstance().sUA?:"JJBugReport/0.1")
            return requestBuilder.build()
        }
    }
}