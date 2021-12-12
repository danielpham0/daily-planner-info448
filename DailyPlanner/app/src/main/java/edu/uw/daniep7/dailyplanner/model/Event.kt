/** Daniel Pham: I wrote the code for this Event data class, used in the event table and
 * parcelized to be passed between fragments* */
package edu.uw.daniep7.dailyplanner.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "event_table")
@Parcelize
data class Event (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val eventType: String,
    val desc: String,
    var origin: String?,
    val address: String,
    val arrivalTime: Int,
    val mode: String,
    var distance: String,
    var duration: Int
) : Parcelable