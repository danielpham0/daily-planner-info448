///** Daniel Pham: I wrote all methods in this Event List View Model, which handles
// *  access to and modification of internal app storage and calls to the Google
// *  Network Service. * */
//package edu.uw.daniep7.dailyplanner.network
//
//import android.content.ContentValues
//import android.content.ContentValues.TAG
//import android.content.Context
//import android.graphics.Color
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import edu.uw.daniep7.dailyplanner.BuildConfig
//import edu.uw.daniep7.dailyplanner.Event
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.io.File
//import java.time.Instant
//import java.time.ZoneId
//import java.time.ZoneOffset
//import java.time.format.DateTimeFormatter
//
//class EventListViewModel: ViewModel() {
//    private val fileName = "events.txt"
//    private val directionsKey: String = BuildConfig.DIRECTIONS_KEY
//    private var _eventListData=  MutableLiveData<List<Event>>()
//    val eventListData: LiveData<List<Event>>
//        get() = _eventListData
//
//    fun getEvents(context: Context) {
//        val eventList = mutableListOf<Event>()
//        val file: File = context.getFileStreamPath(fileName)
//        if (file.exists()) {
//            context.openFileInput(fileName).use {
//                val bufferedReader = it.bufferedReader()
//                val eventsString = bufferedReader.use{it.readText()}
//                val events = eventsString.split("\n")
//                for (event in events) {
//                    val items = event.split("\\ ")
//                    if (items.size === 9) {
//                        val origin = if (items[3] == "null") { null } else items[3]
//                        val eventResult = Event(items[0], items[1], items[2], origin, items[4],
//                            items[5].toInt(),items[6], items[7], items[8].toInt())
//                        eventList.add(eventResult)
//                    }
//                }
//            }
//            eventList.sortBy { it.arrivalTime }
//            _eventListData.value = eventList
//            for (eventPos in 0 until eventList.size) {
//                getDirections(eventPos)
//            }
//        } else _eventListData.value = eventList
//    }
//    // needs to be updated to include position & will need to figure out sorting
//    fun addEvent(context:Context, event: Event) {
//
//        var temp = _eventListData.value?.toMutableList()
//        if (temp === null) {
//            _eventListData.value = mutableListOf()
//            temp = mutableListOf()
//        }
//        temp.add(event)
//        saveEvents(context, temp)
//        _eventListData.value = temp
//        getDirections(temp.size-1)
//    }
//    fun deleteEvent(context:Context, position: Int) {
//        // 1312 NE 45th St Seattle, WA 98105
//        var temp = _eventListData.value!!.toMutableList()
//        var tempOrigin = temp[position].origin
//        temp.removeAt(position)
//        saveEvents(context, temp)
//        _eventListData.value = temp
//        if (temp.size > position) {
//            updateOrigin(context, position, tempOrigin)
//        }
//    }
//
//    fun updateOrigin(context:Context, position: Int, newOrigin: String?) {
//        var temp = _eventListData.value!!.toMutableList()
//        temp[position].origin = newOrigin
//        saveEvents(context, temp)
//        getDirections(position)
//    }
//    fun saveEvents(context: Context, events: List<Event>) {
//        var content: String = ""
//        for (event in events) {
//            val eventContent = listOf(event.title, event.eventType,
//                event.desc, event.origin, event.address,
//                event.arrivalTime,event.mode, event.distance,
//                event.duration)
//            content += eventContent.joinToString("\\ ") + "\n"
//        }
//        try {
//            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
//                it.write(content.toByteArray())
//            }
//        } catch(e: Exception) {
//            Log.e(ContentValues.TAG, "Failure: ${e.message}")
//        }
//    }
//
//    fun getDirections(position: Int) {
//        val curEvent = eventListData.value!![position]
//        val origin = curEvent.origin ?: curEvent.address
//        GoogleApi.retrofitService.getDirections(
//            curEvent.arrivalTime,
//            origin, curEvent.address,
//            curEvent.mode,
//            directionsKey
//        ).enqueue(object: Callback<DirectionsResponse> {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onResponse(
//                call: Call<DirectionsResponse>,
//                response: Response<DirectionsResponse>
//            ) {
//                if (_eventListData.value!!.size <= position) {
//                    return
//                }
//                val body = response.body()!!.routes[0].legs[0]
//                var temp = _eventListData.value!!.toMutableList()
//                temp!![position].distance = body.distance.text
//                temp!![position].duration = body.duration.value
//                _eventListData.value = temp
//            }
//            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                Log.e(ContentValues.TAG, "Failure: ${t.message}")
//            }
//        })
//    }
//}


// IF YOU NEED TO UPDATE ON EVERY INSTANCE

//if (!initialized && events.isNotEmpty()) {
//                for (e in events) {
//                    eventViewModel.getDirectionsAndUpdate(e, e.origin)
//                }
//    data = events.toMutableList()
//    adapter.setData(events)
//    initialized = true
//} else {
//    data = events.toMutableList()
//    adapter.setData(events)
//}