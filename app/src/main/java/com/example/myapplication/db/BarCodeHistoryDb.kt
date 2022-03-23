package com.example.myapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.model.BarCode
import com.example.myapplication.utils.TimeConverter
import kotlinx.coroutines.CoroutineScope

@Database(entities = [BarCode::class], version = 1)
@TypeConverters(TimeConverter::class)
abstract class BarCodeHistoryDb: RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object{
        @Volatile
        var INSTANCE: BarCodeHistoryDb? = null

        fun getDatabaseInstance(context: Context, scope: CoroutineScope): BarCodeHistoryDb {
            val temporaryInstace = INSTANCE
            if (temporaryInstace!=null){
                return temporaryInstace
            }

            synchronized(this){
                val instance = Room.databaseBuilder(context, BarCodeHistoryDb::class.java, "barcodeListdb").build()
                INSTANCE = instance
            return instance
            }
        }
    }


}