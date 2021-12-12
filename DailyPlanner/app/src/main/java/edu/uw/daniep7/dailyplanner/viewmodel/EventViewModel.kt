/** Daniel Pham: I wrote the methods in this view model, which connect the repository
 * to our UI * */
package edu.uw.daniep7.dailyplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import edu.uw.daniep7.dailyplanner.data.EventDatabase
import edu.uw.daniep7.dailyplanner.repository.EventRepository
import edu.uw.daniep7.dailyplanner.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// View model to be implemented in Event List Fragment, and several others.
class EventViewModel(application: Application): AndroidViewModel(application) {
    // Initializes data and repository which are needed to make the view model function
    val readAllData: LiveData<List<Event>>
    private val repository: EventRepository

    init {
        val eventDao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        readAllData = repository.readAllData
    }

    // Add event through just Room
    fun addEvent(event: Event) {
        // Allows the function to be asynchronous and run in the background
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEvent(event)
        }
    }
    // Delete event through just Room
    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }
    // Update event through just Room
    fun updateEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
        }
    }

    // Add events using both Room and Google API
    fun getDirectionsAndAdd(event: Event) {
        repository.getDirectionsAndAdd(viewModelScope, event)
    }

    // Update events using both Room and Google API
    fun getDirectionsAndUpdate(event: Event) {
        repository.getDirectionsAndUpdate(viewModelScope, event)
    }
}