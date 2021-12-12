/** Daniel Pham: I wrote all of the code for this Event Detail Fragment,
 *  which handles onCreate and onCreateView for detailed views of an Event.
 *  These two insert our event data into views from the EventDetail Layout* */
package edu.uw.daniep7.dailyplanner.fragment

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import edu.uw.daniep7.dailyplanner.R
import edu.uw.daniep7.dailyplanner.model.Event
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Fragment for details about a specific movie.
class EventDetailFragment : Fragment() {
    // variables for each item we use to adjust text views and image views properly
    private var event: Event? = null
    private var eventTitle: String? = null
    private var eventAddress: String? = null
    private var eventArrival: String? = null
    private var eventOrigin: String? = null
    private var eventType: String? = null
    private var eventTypeText: String? = null
    private var eventDesc: String? = null
    private var eventMode: String? = null
    private var eventCount: String? = null
    private var eventNum: String? = null

    //handles arguments that are passed in and sets them as our variables for use with views.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dateFormat = DateTimeFormatter.ofPattern("HH:mm, MM/dd/yyyy")
            .withZone(ZoneOffset.UTC)
        arguments?.let{
            event = it.getParcelable("event")
            eventCount = it.getInt("eventCount").toString()
            eventNum = (it.getInt("eventNum") + 1).toString()
            eventTitle = event!!.title
            eventAddress = event!!.address
            eventOrigin = if (eventNum == "1") {
                "Current Location"
            } else {
                event?.origin ?: "Current Location"
            }
            eventArrival = dateFormat.format(Instant.ofEpochSecond(event!!.arrivalTime.toLong())
                .atZone(ZoneId.systemDefault()).toLocalDateTime())
            eventType = event!!.eventType
            eventTypeText = event!!.eventType.replaceFirstChar { it.uppercase() }
            eventDesc = event!!.desc
            eventMode = event!!.mode.replaceFirstChar { it.uppercase() }
        }
    }
    // inflates the views and customizes it to data from the Movie object
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_event_detail, container, false)
        rootView.findViewById<TextView>(R.id.event_detail_title).text = "${eventTitle}"
        rootView.findViewById<ImageView>(R.id.event_detail_banner).setImageURI(
            Uri.parse(
            "android.resource://edu.uw.daniep7.dailyplanner/drawable/"
                    + eventType + "_banner"))
        rootView.findViewById<TextView>(R.id.event_detail_address).text = "Address: $eventAddress"
        rootView.findViewById<TextView>(R.id.event_detail_origin).text = "Origin: $eventOrigin"
        rootView.findViewById<TextView>(R.id.event_detail_num).text = "Event $eventNum of $eventCount"
        rootView.findViewById<TextView>(R.id.event_detail_arr_time).text = "$eventArrival"
        rootView.findViewById<TextView>(R.id.event_detail_type).text = "$eventTypeText"
        rootView.findViewById<TextView>(R.id.event_detail_desc).text = "$eventDesc"
        rootView.findViewById<TextView>(R.id.event_detail_mode).text = "$eventMode"
        rootView.findViewById<Button>(R.id.event_list_button).setOnClickListener{
            findNavController().navigate(R.id.EventListFragment)
        }

        return rootView
    }
}