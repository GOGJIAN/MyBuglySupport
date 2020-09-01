package com.shimao.mybuglylib.data

import android.text.TextUtils
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * @author jian
 *
 */
class LogInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val start = System.currentTimeMillis()
        val response = chain.proceed(request)
        val end = System.currentTimeMillis()

        Log.d("NET_LOG", getLogContent(request, response, end - start))
        return response
    }

    private fun getLogContent(request: Request, response: Response, time: Long): String{
        val result = StringBuilder()
        // request 基本信息
        result.append("net debug log")
                .append("\n========= Request =========")
                .append("\nmethod: ").append(request.method())
                .append("\nurl: ").append(request.url().toString())

        // request headers
        var headers = request.headers()
        result.append("\nheaders {\n")

        for (i in 0 until headers.size()) {
            result.append("\t").append(headers.name(i)).append(": ").append(headers.value(i)).append("\n")

        }
        result.append("}")

        // request post body
        if (TextUtils.equals("POST", request.method())) {
            result.append("\nbody ")
            try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                if(copy.body()!!.contentLength()<10000){
                    copy.body()!!.writeTo(buffer)
                    result.append(buffer.readUtf8())
                }

            } catch (e: IOException) {
                result.append("{print did not work}")
            }
        }

        // response 基本信息
        val peekBody = response.peekBody(Long.MAX_VALUE)
        result.append("\n========= Response =========")

        result.append("\nheaders {\n")
        headers = response.headers()
        for (i in 0 until response.headers().size()) {
            result.append("\t").append(headers.name(i)).append(": ").append(headers.value(i)).append("\n")

        }
        result.append("}")

        result.append("\ncode: ").append(response.code())
                .append("\nmessage: ").append(response.message())
                .append("\njson: ").append(peekBody.string())

        // 时间
        if (result[result.length - 1] != '\n') {
            result.append("\n")
        }
        result.append("========= mTime: ").append(time).append("ms =========")
        return result.toString()
    }


}