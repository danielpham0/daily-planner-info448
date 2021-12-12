/** Daniel Pham: I wrote the code for the Event Database, implementing Room.
 * Standard boiler plate code for instantiating a database. * */

package edu.uw.daniep7.dailyplanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.uw.daniep7.dailyplanner.model.Event

@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class EventDatabase: RoomDatabase(){
    abstract fun eventDao(): EventDao

    companion object{
        // If null, we instantiate and build the database.
        @Volatile
        private var INSTANCE: EventDatabase? = null
        fun getDatabase(context: Context): EventDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}