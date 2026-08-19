package com.mapmory.shared.data.local.photo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoMetadataEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PhotoMetadataDatabase : RoomDatabase() {
    abstract fun photoMetadataDao(): PhotoMetadataDao

    companion object {
        @Volatile
        private var instance: PhotoMetadataDatabase? = null

        fun getInstance(context: Context): PhotoMetadataDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PhotoMetadataDatabase::class.java,
                "mapmory-photo-metadata.db",
            ).build().also { instance = it }
        }
    }
}
