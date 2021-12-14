/** Daniel Pham: I wrote the methods in this repository, which connect different datasources
 * and can be used in our view model to be displayed in the UI * */

package edu.uw.daniep7.dailyplanner.repository;

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import edu.uw.daniep7.dailyplanner.BuildConfig
import edu.uw.daniep7.dailyplanner.data.EventDao
import edu.uw.daniep7.dailyplanner.model.Event
import edu.uw.daniep7.dailyplanner.network.DirectionsResponse
import edu.uw.daniep7.dailyplanner.network.GoogleApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Our repository for events which connects Room Database and Google API (DP)
class EventRepository(private val eventDao: EventDao) {
    // Our API Key
    private val directionsKey: String = BuildConfig.DIRECTIONS_KEY
    val readAllData: LiveData<List<Event>> = eventDao.readAllData()

    // Add Event through just DAO
    suspend fun addEvent(event: Event) {
        eventDao.addEvent(event)
    }

    // Update Event through just DAO
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    // Delete Event through just DAO
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    // Connects the Google API to Room and finds the directions before adding an event.
    fun getDirectionsAndAdd(scope: CoroutineScope, event: Event) {
        val origin = event.origin ?: event.address
        GoogleApi.retrofitService.getDirections(
            event.arrivalTime,
            origin, event.address,
            event.mode,
            directionsKey
        ).enqueue(object: Callback<DirectionsResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body()!!.routes[0] != null) {
                    val body = response.body()!!.routes[0].legs[0]
                    event.distance = body.distance.text
                    event.duration = body.duration.value
                    Log.v(TAG, "Directions Call: ${event}")
                    scope.launch(Dispatchers.IO) {
                        eventDao.addEvent(event)
                    }
                }
            }
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "Failure: ${t.message}")
            }
        })
    }

    // Connects the Google API to Room and finds the directions before updating an event.
    fun getDirectionsAndUpdate(scope: CoroutineScope, event: Event) {
        val origin = event.origin ?: event.address
        GoogleApi.retrofitService.getDirections(
            event.arrivalTime,
            origin, event.address,
            event.mode,
            directionsKey
        ).enqueue(object: Callback<DirectionsResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body()!!.routes[0] != null) {
                    val body = response.body()!!.routes[0].legs[0]
                    event.distance = body.distance.text
                    event.duration = body.duration.value
                    Log.v(TAG, "Directions Call: ${event}")
                    scope.launch(Dispatchers.IO) {
                        eventDao.updateEvent(event)
                    }
                }
            }
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e(ContentValues.TAG, "Failure: ${t.message}")
            }
        })
    }
}