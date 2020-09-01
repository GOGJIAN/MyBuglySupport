package com.shimao.mybuglylib.util

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.WindowManager
import com.shimao.mybuglylib.core.JJBugReport
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author : jian
 * @date   : 2020/7/17 15:40
 * @version: 1.0
 */
object PublicParams {

    var cver:String = ""
    var imei: String? = ""
    var Android_ID = ""
    var device_id = ""

    /*手机品牌*/
    private var device_merchant:String = ""
    private var device_model:String = ""
    private var device_version:String = ""


    /*屏幕分辨率*/
    private var width: Int = 0
    var height: Int = 0
    private var densityDpi: Int = 0

    fun getPublicParams(): MutableMap<String, String>{
        val map = mutableMapOf<String,String>()
        map["cver"] = cver
        map["via"] = "android"
        map["channel_id"] = "3"
        map["source"] = "mob"
        map["imei"] = imei?:""
        map["app"] = JJBugReport.getInstance().sApplication!!
        map["ratio"] = "$width*$height"
        map["device_model"] = device_model
        map["device_merchant"] = device_merchant
        map["device_version"] = device_version
        map["environment"] = if(JJBugReport.getInstance().sIsDebug)"debug" else "release"

        map["net_type"] = getNetworkType(JJBugReport.getInstance().sContext!!)
        map["device_id"] = device_id
        return map
    }

    fun retrievePublicInfo(context: Context){
        retrieveImei(context)
        retrieveVersion(context)
        initWidhei(context)
        device_merchant = Build.BRAND.trim { it <= ' ' }.replace(" ".toRegex(), "")
        device_model = Build.MODEL.trim { it <= ' ' }.replace(" ".toRegex(), "")
        device_version = Build.VERSION.RELEASE.trim { it <= ' ' }.replace(" ".toRegex(), "")
        if(PreferenceUtil.getStringValue(context,"device_id","")=="") {
            device_id = "h_" + toMd5(imei + Android_ID)
            PreferenceUtil.setStringValue(context,"device_id", device_id)
        }else{
            device_id = PreferenceUtil.getStringValue(context,"device_id","")
        }
    }

    fun toMd5(url: String): String {
        try {
            if (!TextUtils.isEmpty(url)) {
                val messageDigest = MessageDigest.getInstance("MD5")
                val md5bytes = messageDigest.digest(url.toByteArray())
                return bytesToHexString(md5bytes,false)
            }
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        }

        return ""
    }

    private val DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

    private val UPPER_CASE_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')


    private fun bytesToHexString(bytes: ByteArray, upperCase: Boolean): String {
        val digits = if (upperCase) UPPER_CASE_DIGITS else DIGITS
        val buf = CharArray(bytes.size * 2)
        var c = 0
        for (b in bytes) {
            buf[c++] = digits[(b.toInt().shr(4)) and 0xf]
            buf[c++] = digits[b.toInt() and 0xf]
        }
        return String(buf)
    }

    private fun initWidhei(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metric = DisplayMetrics()
        //        context.getResources().getDisplayMetrics();
        wm.defaultDisplay.getRealMetrics(metric)
        width = metric.widthPixels     // 屏幕宽度（像素）
        height = metric.heightPixels   // 屏幕高度（像素）
        densityDpi = metric.densityDpi
    }


    /**
     * 获取设备imei号码
     *
     * @param context
     * @return
     */
    fun retrieveImei(context: Context) {// 上传imei
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //android.provider.Settings;
        imei = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)

    }


    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    fun retrieveVersion(context: Context) {
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            val version = info.versionName
            cver = version
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取当前网络类型
     */
    fun getNetworkType(context: Context): String { //        结果返回值
        var netType = "NONE"

        //        获取手机所有连接管理对象
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //        获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo ?: return netType
        //        NetworkInfo对象为空 则代表没有网络
        //        否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        val nType = networkInfo.type
        if (nType == ConnectivityManager.TYPE_WIFI) { //            WIFI
            netType = "WIFI"
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            val nSubType = networkInfo.subtype
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            netType = if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                && !telephonyManager.isNetworkRoaming
            ) { //                4G 网络
                "4G"
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA || (nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        && !telephonyManager.isNetworkRoaming)
            ) { //                3G网络   联通的3G为UMTS或HSDPA 电信的3G为EVDO
                "3G"
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS || nSubType == TelephonyManager.NETWORK_TYPE_EDGE || (nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                        && !telephonyManager.isNetworkRoaming)
            ) { //                2G网络 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
                "2G"
            } else if(nSubType == 20){
                "5G"
            }else{
                "NO DISPLAY"
            }
        }
        return netType
    }
}