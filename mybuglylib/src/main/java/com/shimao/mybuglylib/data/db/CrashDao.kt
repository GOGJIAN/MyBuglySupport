package com.shimao.mybuglylib.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query


/**
 * @author : jian
 * @date   : 2020/7/21 18:04
 * @version: 1.0
 */
@Dao
interface CrashDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: CrashVO)

    @Query("DELETE FROM CrashVO WHERE status = 1")
    fun deleteAlreadyPost()

    @Query("UPDATE CrashVO SET status = 1 WHERE id =:id")
    fun updateStatusById(id:String)

    @Query("SELECT * FROM CrashVO WHERE status = 0")
    fun getAllUnPostData():List<CrashVO>
}