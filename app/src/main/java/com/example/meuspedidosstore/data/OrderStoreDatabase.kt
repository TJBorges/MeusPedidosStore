package com.example.meuspedidosstore.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Archived::class], version = 1, exportSchema = false)
abstract class OrderStoreDatabase : RoomDatabase() {

    abstract fun archivedDao(): ArchivedDao

    companion object {
        @Volatile
        private var INSTANCE: OrderStoreDatabase? = null

        fun getDatabase(context: Context): OrderStoreDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrderStoreDatabase::class.java,
                    "orderStoreDatabase"
                ).build()
                INSTANCE = instance
                return instance
            }

        }
    }
}