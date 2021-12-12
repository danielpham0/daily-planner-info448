package edu.uw.daniep7.dailyplanner.data

import androidx.lifecycle.LiveData
import androidx.room.*
import edu.uw.daniep7.dailyplanner.model.Event

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEvent (event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM event_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Event>>
}