/** Daniel Pham: I wrote the code for this Event List Recycler View
 *  utilized by the Event List Fragment.
 *  The Recycler view is made to handle 3 different scenarios & displays different
 *  information based on that:
 *      - Post-Departure Time & first item in the list
 *      - Post-Departure Time & not the first item in the list
 *      - Pre-Departure Time * */
package edu.uw.daniep7.dailyplanner.adapters

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import edu.uw.daniep7.dailyplanner.R
import edu.uw.daniep7.dailyplanner.ReminderBroadcast
import edu.uw.daniep7.dailyplanner.model.Event
import edu.uw.daniep7.dailyplanner.viewmodel.EventViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Recycler view for List of Events
class EventAdapter(private val navController: NavController, private val viewModel: EventViewModel) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    private var data = emptyList<Event>()
    private lateinit var context: Context

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // initialize all the components we are working with
        val eventItemRow: LinearLayout = view.findViewById(R.id.event_item_row)
        val eventType: ImageView = view.findViewById(R.id.event_type_icon)
        val eventTitle: TextView = view.findViewById(R.id.event_item_title)
        val eventAddress: TextView = view.findViewById(R.id.event_item_address)
        val eventDesc: TextView = view.findViewById(R.id.event_item_desc)
        val eventValOne: TextView = view.findViewById(R.id.event_item_val_one)
        val eventValTwo: TextView = view.findViewById(R.id.event_item_val_two)
        val eventDeleteButton: ImageButton = view.findViewById(R.id.event_item_delete_button)
        val eventLabelOne: TextView = view.findViewById(R.id.event_item_label_one)
        val eventLabelTwo: TextView = view.findViewById(R.id.event_item_label_two)
    }

    // Inflates for each row of data utilizing event_item_layout as its format
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.event_item_layout, viewGroup, false)
        context = viewGroup.context
        return ViewHolder(view)
    }

    // Binds event specific data and assigns values to Views
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val resultItem = data[position]

        // INITIALIZE BASICS FOR EVENT LAYOUT
        viewHolder.eventTitle.text = resultItem.title
        viewHolder.eventAddress.text = resultItem.address
        viewHolder.eventType.setImageURI(
            Uri.parse(
            "android.resource://edu.uw.daniep7.dailyplanner/drawable/"
                    + resultItem.eventType + "_icon"))

        // INITIALIZE BUTTONS FOR EVENT LAYOUT
        // Makes the row a button to lead to detail
        viewHolder.eventItemRow.setOnClickListener {
            val argBundle = Bundle()
            // insert event number and total number of events, mode for travel
            argBundle.putParcelable("event", resultItem)
            argBundle.putInt("eventNum", position)
            argBundle.putInt("eventCount", data.size)
            // Directs us to detail fragment after clicking on the event fragment
            navController.navigate(R.id.EventDetailFragment, argBundle)
        }
        // Delete button to get rid of the recycler item
        viewHolder.eventDeleteButton.setOnClickListener {
            // Creates an alert dialog for us to confirm
            val builder = AlertDialog.Builder(context)
            builder.setPositiveButton("Yes") { _, _ ->
                // On delete, send message to view model, and then give this objects origin
                // to the next object in line.
                viewModel.deleteEvent(resultItem)
                if (data.size-1 > position) {
                    data[position+1].origin = resultItem.origin
                    viewModel.getDirectionsAndUpdate(data[position+1])
                }
                // Delete the notification that was previously set
                val intent = Intent(context, ReminderBroadcast::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context,
                    resultItem.arrivalTime.toInt(), intent,
                    PendingIntent.FLAG_CANCEL_CURRENT)
                val alarmManager = context?.applicationContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()

                Toast.makeText(
                    context,
                    "Successfully removed: ${resultItem.title}.",
                    Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("No") { _, _ -> }
            builder.setTitle("Delete event?")
            builder.setMessage("Are you sure you want to delete '${resultItem.title}?'")
            builder.create().show()
        }

        // CALCULATE TIMINGS AND REPLACE LABELS AND VALUES BASED ON EVENT POSITION AND TIME
        // Estimated Arrivals and Departures, with Epoch conversions
        val dateFormat = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneOffset.UTC)
        val arrTime = dateFormat.format(
            Instant.ofEpochSecond(resultItem.arrivalTime)
            .atZone(ZoneId.systemDefault()).toLocalDateTime())
        val depTime = dateFormat.format(
            Instant.ofEpochSecond((resultItem.arrivalTime -
                resultItem.duration))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime())
        val curTime = System.currentTimeMillis()/1000
        val minBeforeDep = (resultItem.arrivalTime - resultItem.duration - curTime) / 60
        val estArr = dateFormat.format(
            Instant.ofEpochSecond((
                System.currentTimeMillis()/1000 + resultItem.duration))
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime())
        // Setting the views to those values
        viewHolder.eventDesc.text = resultItem.distance + " " + resultItem.mode
        viewHolder.eventValOne.text = depTime
        viewHolder.eventValTwo.text = arrTime
        viewHolder.eventItemRow.setBackgroundColor(Color.parseColor("#B2B2B2"))
        viewHolder.eventLabelOne.text = "Departure"
        viewHolder.eventLabelTwo.text = "Arrival"
        // Change recycler view formatting based on how far away from departure the event is
        if (minBeforeDep in 1..59) {
            viewHolder.eventValOne.text = "$minBeforeDep min"
        }
        else if (minBeforeDep <= 0) {
            if (viewHolder.adapterPosition == 0) {
                viewHolder.eventDesc.text = "You are on your way!"
                viewHolder.eventLabelOne.text = "Distance"
                viewHolder.eventLabelTwo.text = "Est. Arrival"
                viewHolder.eventValOne.text = resultItem.distance
                viewHolder.eventValTwo.text = estArr
                viewHolder.eventItemRow.setBackgroundColor(Color.parseColor("#303030"))
            } else {
                viewHolder.eventDesc.text = "Waiting on first event."
                viewHolder.eventValOne.text = "Waiting"
                viewHolder.eventValTwo.text = "Waiting"
                viewHolder.eventItemRow.setBackgroundColor(Color.parseColor("#505050"))
            }
        }
    }
    // Necessary to implement the recycler viewer interface
    override fun getItemCount() = data.size

    // Allows us to set the data from the outside.
    fun setData(events: List<Event>) {
        this.data = events
        notifyDataSetChanged()
    }
}