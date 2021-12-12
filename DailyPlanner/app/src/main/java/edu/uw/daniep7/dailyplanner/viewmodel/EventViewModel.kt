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

class EventViewModel(application: Application): AndroidViewModel(application) {
    val readAllData: LiveData<List<Event>>
    private val repository: EventRepository

    init {
        val eventDao = EventDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        readAllData = repository.readAllData
    }

    fun addEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEvent(event)
        }
    }
    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
        }
    }
    fun updateEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEvent(event)
        }
    }

    fun getDirectionsAndAdd(event: Event) {
        repository.getDirectionsAndAdd(viewModelScope, event)
    }

    fun getDirectionsAndUpdate(event: Event) {
        repository.getDirectionsAndUpdate(viewModelScope, event)
    }
}