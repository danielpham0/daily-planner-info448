/** Daniel Pham: I wrote the code for this Event List Fragment,
 *  and the Event List Recycler View utilized by the Fragment.
 *  The Event List Fragment includes a recycler view, current location listener
 *  and view model object - all of which I set up. * */

package edu.uw.daniep7.dailyplanner

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import edu.uw.daniep7.dailyplanner.adapters.EventAdapter
import edu.uw.daniep7.dailyplanner.model.Event
import edu.uw.daniep7.dailyplanner.viewmodel.EventViewModel

// Fragment for the main menu, and a list of events which updates based on current location. (DP)
class EventListFragment : Fragment() {
    // Instantiate all variables before Creating the views
    var data : MutableList<Event> = mutableListOf()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var adapter: EventAdapter
    private lateinit var eventViewModel: EventViewModel

    // Initializes all of the functional necessities of the fragment, and inflates the view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_event_list, container, false)

        // Start a notification channel, should be moved to add event later
        createNotificationChannel()

        // Instantiate View Model and connect it to the adapter
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        eventViewModel.readAllData.observe(viewLifecycleOwner, Observer { events ->
            // sorts the list based on the arrival times of the events
            data = events.toMutableList()
            data.sortBy{it.arrivalTime}
            adapter.setData(data)
        })

        // Instantiate the Adapter with proper spacing
        adapter = EventAdapter(findNavController(), eventViewModel)
        val recycler = rootView.findViewById<RecyclerView>(R.id.recycler_list)
        recycler.layoutManager = LinearLayoutManager(activity)
        recycler.adapter = adapter
        val divider = DividerItemDecoration(
            recycler.context,
            LinearLayoutManager.VERTICAL
        )
        recycler.addItemDecoration(divider)

        // Instantiate location callback and listener, which updates every 3 minutes.
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
                        data[0].origin = locCoordinates
                        eventViewModel.getDirectionsAndUpdate(data[0])
                    }
                }
            }
        }
        requestLocationUpdates(locationRequest)

        // Button for moving to the Add Event activity.
        val goToSecondActivity = Intent(activity, AddEventActivity::class.java)
        rootView.findViewById<Button>(R.id.add_event_button).setOnClickListener{
            startActivity(goToSecondActivity)
        }
        return rootView
    }

    override fun onStop() {
        super.onStop()
        // Don't need to check for updates when the app is closed.
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Helper function to create a custom channel for Event notifications
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "EventReminderChannel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notifyEvent", name, importance).apply {
                description = "Channel for Daily Planner events."
            }
            val notificationManager: NotificationManager = context?.applicationContext
                ?.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestLocationUpdates(locationRequest: LocationRequest) {
        // Check if the permissions have already been granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Send request for permissions and act based on the request
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when {
                        permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                            // Precise location access granted.
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        }
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                            // Only approximate location access granted.
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                        } else -> {
                        // No location access granted.
                    }
                    }
                }
            }
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}