package com.example.dentalprueba.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dentalprueba.converters.Converters
import com.example.dentalprueba.model.Patient

@Database(entities = [Patient::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PatientDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao

    companion object {
        @Volatile
        private var INSTANCE: PatientDatabase? = null

        fun getDatabase(context: Context): PatientDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PatientDatabase::class.java,
                    "patient_database"
                )
                .fallbackToDestructiveMigration() // For simplicity in dev phase
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
