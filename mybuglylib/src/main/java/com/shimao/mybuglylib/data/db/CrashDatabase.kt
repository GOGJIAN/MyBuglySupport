package com.shimao.mybuglylib.data.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context


/**
 * @author : jian
 * @date   : 2020/7/21 18:06
 * @version: 1.0
 */
@Database(entities = [CrashVO::class],version = 4)
abstract class CrashDatabase: RoomDatabase() {
    abstract fun crashDao(): CrashDao

    companion object {
        private var instance: CrashDatabase? = null

        fun init(context: Context) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                CrashDatabase::class.java,
                "crashdatabase"
            )
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
        }

        fun get(): CrashDatabase {
            return instance!!
        }

    }
}