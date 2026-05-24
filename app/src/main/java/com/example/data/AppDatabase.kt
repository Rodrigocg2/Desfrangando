package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.model.SavedWorkout
import com.example.model.WorkoutHistory
import com.example.model.WeightHistory

@Database(entities = [SavedWorkout::class, WorkoutHistory::class, WeightHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
