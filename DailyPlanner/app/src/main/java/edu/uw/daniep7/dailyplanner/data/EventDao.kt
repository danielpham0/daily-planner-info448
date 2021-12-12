/** Daniel Pham: I wrote the code for the Event Dao to allow the repository
 * to connect to the database. * */

package edu.uw.daniep7.dailyplanner.data

import androidx.lifecycle.LiveData
import androidx.room.*
import edu.uw.daniep7.dailyplanner.model.Event

// Standard Dao Interface
@Dao
interface EventDao {
    // Built in tag for insert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEvent (event: Event)

    // Built in tag for update
    @Update
    suspend fun updateEvent(event: Event)

    // Built in tag for delete
    @Delete
    suspend fun deleteEvent(event: Event)

    // SQL Query to retrieve all rows from event_table, ordered by their id
    @Query("SELECT * FROM event_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<Event>>
}