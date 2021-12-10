/** Daniel Pham: I wrote all methods and data classes in this Google Network Service,
 * which connects our View Model to Google Map's Directions API and translates it into
 * a format that is usable using Moshi. * */
package edu.uw.daniep7.dailyplanner.network

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
// URL for our Movie API
private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

interface GoogleApiService {
    @GET("directions/json")
    fun getDirections(@Query("arrival_time") arrival_time: Int,
                      @Query("origin") origin: String,
                      @Query("destination") destination: String,
                      @Query("mode") mode: String,
                      @Query("key") key: String):Call<DirectionsResponse>
}

// initialize moshi
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

//initialize retrofit
private val retrofit = Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

// Uses retrofit to create usable api object
object GoogleApi{
    val retrofitService: GoogleApiService by lazy {
        retrofit.create(GoogleApiService::class.java)
    }
}

// Data classes for us to work with the JSON we get returned
data class DirectionsResponse(
    var routes: List<Route>
)
data class Route(
    var legs: List<Leg>
)
data class Leg(
    var distance: Distance,
    var duration: Duration,
)
data class Distance(
    var text: String,
    var value: Int
)
data class Duration(
    var text: String,
    var value: Int
)