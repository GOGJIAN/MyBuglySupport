package com.shimao.mybugly

import android.app.Application
import com.shimao.mybuglylib.core.JJBugReport

/**
 * @author : jian
 * @date   : 2020/7/17 11:50
 * @version: 1.0
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        JJBugReport
            .getInstance()
            .baseUrl("https://m.lehe.com/api/jarvis/record/logger")
            .applicationName("demo")
            .packetName("com.shimao.mybugly")
            .delay(250)
            .mainActivity(MainActivity2::class.java.name)
            .init(this)
    }
}