package com.shimao.mybuglylib.data.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * @author : jian
 * @date   : 2020/7/21 17:51
 * @version: 1.0
 */
@Entity
class CrashVO (
    @PrimaryKey
    val id:String,
    val message:String,
    val exception: String,
    val stack:String,
    val activitys:String,
    val fragments:String,
    val clicks:String,
    val urls:String,
    val ctime:Long,
    val status:Int//0未上报，1已上报
)

