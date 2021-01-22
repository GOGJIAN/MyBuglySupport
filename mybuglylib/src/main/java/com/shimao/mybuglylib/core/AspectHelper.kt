package com.shimao.mybuglylib.core

import android.content.res.Resources
import android.support.v4.app.Fragment
import android.util.Log
import android.util.LruCache
import android.view.View
import com.shimao.mybuglylib.data.model.ActivityEvent
import com.shimao.mybuglylib.data.model.ClickEvent
import com.shimao.mybuglylib.data.model.FragmentEvent
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import java.lang.Exception


/**
 * @author : jian
 * @date   : 2020/8/3 10:06
 * @version: 1.0
 */
@Aspect
class AspectHelper {
    private val TAG = this.javaClass.simpleName

//    private val activityList = mutableListOf("onCreate","onResume","onPause","onDestroy")
//    @After("execution(* android.app.Activity.on**(..))")
//    fun onActivityRecord(joinPoint: JoinPoint) {
//        val key = joinPoint.signature.toString()
//        val clazz = joinPoint.getThis()::class.java.name
//        if(!(clazz.startsWith(JJBugReport.getInstance().sSonPacketName)
//                    &&(key.contains("androidx.fragment.app.FragmentActivity")))){
//            return
//        }
//        var count = 0
//        for (str in activityList){
//            if(joinPoint.signature.name == str){
//                count++
//            }
//        }
//        if(count == 0){
//            return
//        }
//        JJBugReport.getInstance().addActivityRecord(
//            ActivityEvent(System.currentTimeMillis(),clazz,joinPoint.signature.name)
//        )
//    }

    private val fragmentList = mutableListOf("onResume","onPause","onHiddenChanged")
    @Before("execution(* android.support.v4.app.Fragment.on**(..))")
    fun onFragmentRecord(joinPoint: JoinPoint){
        val key = joinPoint.signature.toString()
        val clazz = joinPoint.getThis()::class.java.name
        if(!(clazz.startsWith(JJBugReport.getInstance().sSonPacketName)&&key.contains("android.support.v4.app.Fragment"))){
            return
        }
        var count = 0
        for (str in fragmentList){
            if(joinPoint.signature.name == str){
                count++
            }
        }
        if(count == 0){
            return
        }

        val status = if(joinPoint.signature.name == "onHiddenChanged"&&joinPoint.args.isNotEmpty() && joinPoint.args[0] is Boolean){
            "_"+joinPoint.args[0].toString()
        }else ""

        val fragment = joinPoint.getThis() as Fragment
        val index = try {
            fragment.fragmentManager!!.fragments.indexOf(fragment)
        }catch (e:Exception){
            -1
        }
        if(index==-1)
            return
        JJBugReport.getInstance().addFragmentRecord(
            FragmentEvent(System.currentTimeMillis(),clazz+"_"+index,joinPoint.signature.name+" "+status+fragment.activity)
        )
    }

    @Before("execution(* android.support.v4.app.Fragment.setUserVisibleHint(..))")
    fun onFragmentShow(joinPoint: JoinPoint){
        val key = joinPoint.signature.toString()
        val clazz = joinPoint.getThis()::class.java.name
        if(!(clazz.startsWith(JJBugReport.getInstance().sSonPacketName)&&key.contains("android.support.v4.app.Fragment"))){
            return
        }
        val fragment = joinPoint.getThis() as Fragment
        val index = try {
            fragment.fragmentManager!!.fragments.indexOf(fragment)
        }catch (e:Exception){
            -1
        }
        if(index==-1)
            return
        if(joinPoint.args.isNotEmpty() && joinPoint.args[0] is Boolean){
            JJBugReport.getInstance().addFragmentRecord(
                FragmentEvent(System.currentTimeMillis(),clazz+"_"+index,joinPoint.signature.name+"_"+joinPoint.args[0]+" "+fragment.activity)
            )
        }
    }

    @Before("execution(* android.view.View.OnClickListener.onClick(..))")
    fun onClick(joinPoint: JoinPoint){
        val key = joinPoint.signature.toString()
        if(joinPoint.args.isNotEmpty() && joinPoint.args[0] is View){
            val view = joinPoint.args[0] as View
            val id:String = try {
                view.context!!.resources.getResourceEntryName(view.id)
            }catch (e: Resources.NotFoundException){
                "null"
            }
            JJBugReport.getInstance().addClickEvent(
                ClickEvent(System.currentTimeMillis(),view.context::class.java.name,id)
            )
        }
    }
}