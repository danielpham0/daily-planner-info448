/** Daniel Pham: I wrote the methods in this view model, which connect the repository
 * to our UI * */
package edu.uw.daniep7.dailyplanner.viewmodel

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import edu.uw.daniep7.dailyplanner.BuildConfig
import edu.uw.daniep7.dailyplanner.data.EventDatabase
import edu.uw.daniep7.dailyplanner.repository.EventRepository
import edu.uw.daniep7.dailyplanner.model.Event
import edu.uw.daniep7.dailyplanner.network.GoogleApi
import edu.uw.daniep7.dailyplanner.network.PlaceResult
import edu.uw.daniep7.dailyplanner.network.PlacesResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// View model to be implemented in Event List Fragment, and several others.
class EventViewModel(application: Application): AndroidViewModel(application) {
    // Initializes data and repository which are needed to make the view model function
    val readAllData: LiveData<List<Event>>
    private val repository: EventRepository

    private val directionsKey: String = BuildConfig.DIRECTIONS_KEY
    private var _placeData = MutableLiveData<List<PlaceResult>>()
    val placeData: LiveData<List<PlaceResult>>
        get() = _placeData

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

    // Connects the Google API to Room and finds the directions before updating an event.
    fun getListOfPlaces(query: String, location: String?) {
        val loc = location ?: "47.6062,122.3321"
        GoogleApi.retrofitService.getPlaces(
            query,
            loc,
            directionsKey
        ).enqueue(object: Callback<PlacesResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<PlacesResponse>,
                response: Response<PlacesResponse>
            ) {
                if (response.body()!!.results != null) {
                    val body = response.body()!!.results
                    _placeData.value = body
                }
            }
            override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "Failure: ${t.message}")
            }
        })
    }
}