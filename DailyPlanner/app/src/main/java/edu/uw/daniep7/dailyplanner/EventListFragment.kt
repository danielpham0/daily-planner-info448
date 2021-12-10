/** Daniel Pham: I wrote all of the code for this Event List Fragment,
 *  and the Event List Recycler View utilized by the Fragment.
 *  The Event List Fragment includes a recycler view, current location listener
 *  and view model object - all of which I set up.
 *  The Recycler view is made to handle 3 different scenarios & displays different
 *  information based on that:
 *      - Post-Departure Time & first item in the list
 *      - Post-Departure Time & not the first item in the list
 *      - Pre-Departure Time * */
package edu.uw.daniep7.dailyplanner

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import edu.uw.daniep7.dailyplanner.network.EventListViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class EventListFragment : Fragment() {
    private val tempData: MutableList<Event> = mutableListOf(
        Event(
            "Go to MGH", "school",
            "Desc 1.2", null, "1851 NE Grant Ln, Seattle, WA 98105",
            1639161592, "walking", "Undefined", -1
        ),
        Event(
            "Go to TP Tea", "food",
            "Desc 2", "1851 NE Grant Ln, Seattle, WA 98105",
            "1312 NE 45th St Seattle, WA 98105",
            1639161592, "walking", "Undefined", -1
        ),
        Event(
            "Go to H Mart", "shopping",
            "Desc 3", "1312 NE 45th St Seattle, WA 98105",
            "4216 University Way NE, Seattle, WA 98105",
            1639164592, "walking", "Undefined", -1
        )
    )
    private var tempCount: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var data : MutableList<Event> = mutableListOf()
    private lateinit var adapter: EventAdapter
    private lateinit var viewModel: ViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_event_list, container, false)
        viewModel = ViewModelProvider(this).get(EventListViewModel::class.java)
        val observer = Observer<List<Event>> {
            data.clear()
            data.addAll(it)
            adapter?.let{
                it.notifyDataSetChanged()
            }
        }
        (viewModel as EventListViewModel).eventListData.observe(viewLifecycleOwner, observer)

        // Grab event data from internal storage
        (viewModel as EventListViewModel).getEvents(requireContext())

        // adapter content
        adapter = EventAdapter(data, findNavController(), viewModel as EventListViewModel)
        val recycler = rootView.findViewById<RecyclerView>(R.id.recycler_list)
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.adapter = adapter
        val divider = DividerItemDecoration(
            recycler.context,
            LinearLayoutManager.VERTICAL
        )
        recycler.addItemDecoration(divider)

        // location listener
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationRequest = LocationRequest().apply {
            interval = 300000
            fastestInterval = 180000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                var locCoordinates: String?
                if (locationResult != null) {
                    locCoordinates = "${locationResult.lastLocation.latitude}," +
                            "${locationResult.lastLocation.longitude}"
                    if (data.size >= 1) {
                        (viewModel as EventListViewModel).updateOrigin(
                            requireContext(), 0, locCoordinates)
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

//        val goToSecondActivity = Intent(activity, SecondActivity::class.java)
        rootView.findViewById<Button>(R.id.add_event_button).setOnClickListener{
//            startActivity(goToSecondActivity)
            Toast.makeText(activity, "Will connect to add event activity!",
                Toast.LENGTH_LONG).show();
            (viewModel as EventListViewModel).addEvent(requireContext(), tempData[tempCount])
            if (tempCount + 1 < tempData.size) tempCount +=1
        }
        return rootView
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

// each time we have an update on location, we want to update origin for nulls
// and we also want to be able to check the time to see if we need to
// maybe an event listener for location updates --> updates loc if loc is null
class EventAdapter(private val data: MutableList<Event>, private val navController: NavController,
                   private val viewModel: EventListViewModel) :
    RecyclerView.Adapter<EventAdapter.ViewHolder>() {
    // List of the main views we will be altering
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
        val eventMoreButton: ImageButton = view.findViewById(R.id.event_item_more_button)
        val eventLabelOne: TextView = view.findViewById(R.id.event_item_label_one)
        val eventLabelTwo: TextView = view.findViewById(R.id.event_item_label_two)
    }

    // Inflates for each row of data utilizing state_row_layout as its format
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.event_item_layout, viewGroup, false)
        context = viewGroup.context
        return ViewHolder(view)
    }

    // binds movie specific data and assigns values to Views
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // change views to match with the data
        val resultItem = data[position]
        viewHolder.eventTitle.text = resultItem.title
        viewHolder.eventAddress.text = resultItem.address
        viewHolder.eventType.setImageURI(Uri.parse(
            "android.resource://edu.uw.daniep7.dailyplanner/drawable/"
                    + resultItem.eventType + "_icon"))
        // detail button
        viewHolder.eventItemRow.setOnClickListener {
            val argBundle = Bundle()
            // insert event number and total number of events, mode for travel
            argBundle.putParcelable("event", resultItem)
            argBundle.putInt("eventNum", position)
            argBundle.putInt("eventCount", data.size)
            // Directs us to detail fragment after clicking on the event fragment
            navController.navigate(R.id.EventDetailFragment, argBundle)
        }
        // delete button to get rid of the recycler item
        viewHolder.eventMoreButton.setOnClickListener {
            viewModel.deleteEvent(context, position)
        }

        // Estimated Arrivals and Departures, with Epoch conversions
        val dateFormat = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneOffset.UTC)
        val arrTime = dateFormat.format(Instant.ofEpochSecond(resultItem.arrivalTime.toLong())
            .atZone(ZoneId.systemDefault()).toLocalDateTime())
        val depTime = dateFormat.format(Instant.ofEpochSecond((resultItem.arrivalTime -
                resultItem.duration).toLong())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime())
        val curTime = System.currentTimeMillis()/1000
        val minBeforeDep = (resultItem.arrivalTime - resultItem.duration - curTime) / 60
        val estArr = dateFormat.format(Instant.ofEpochSecond((
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
    override fun getItemCount() = data.size
}