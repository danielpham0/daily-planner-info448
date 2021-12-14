/** Daniel Pham: I wrote the code for the Add Event Activity, which is a form with
 * multiple complex options to choose from including: searched addresses and time
 * of arrival. On submission, it adds the event and reorganizes origin addresses.* */
package edu.uw.daniep7.dailyplanner

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.uw.daniep7.dailyplanner.model.Event
import edu.uw.daniep7.dailyplanner.viewmodel.EventViewModel
import androidx.lifecycle.Observer
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.uw.daniep7.dailyplanner.network.PlaceResult
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// The second activity for our application which handles the UI for adding a new event (DP)
class AddEventActivity : AppCompatActivity() {
    // Initialize all necessary variables
    private lateinit var eventViewModel: EventViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // All baseline submission values
    private lateinit var type: String
    private lateinit var mode: String
    private var eventData = mutableListOf<Event>()
    private var placeData = mutableListOf<PlaceResult>()
    private var hour = 0
    private var min = 0
    private var arrivalTime: Long? = null
    private var userLocation: String? = null
    private var address: String? = null

    // Once the activity is booted up, initialize all services and assign values necessary
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // HANDLE RETRIEVING THE LAST LOCATION IF WE HAVE PERMISSIONS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { locationResult : Location? ->
                    if (locationResult != null) {
                        userLocation = "${locationResult.latitude}," +
                                "${locationResult.longitude}"
                    }}
        }

        // INITIALIZE ALL SPINNERS WITH THEIR REQUIRED VALUES
        // ADDRESS SPINNER
        val addressSpinner = findViewById<Spinner>(R.id.event_addr_spinner)
        val addressList = mutableListOf<String>()
        val addressAdapter = ArrayAdapter(this,
            R.layout.spinner_item, addressList)
        addressSpinner.adapter = addressAdapter
        addressSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                address = addressList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@AddEventActivity,
                    "Please select an address.", Toast.LENGTH_SHORT).show()
            }
        }
        // TYPE SPINNER
        val typeSpinner = findViewById<Spinner>(R.id.event_type_spinner)
        val typeList = listOf("Home", "Work", "School", "Food", "Meeting", "Shopping", "Other")
        val typeAdapter = ArrayAdapter(this,
            R.layout.spinner_item, typeList)
        typeSpinner.adapter = typeAdapter
        typeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                type = typeList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@AddEventActivity,
                    "Please select a type.", Toast.LENGTH_SHORT).show()
            }
        }
        // MODE SPINNER
        val modeSpinner = findViewById<Spinner>(R.id.event_mode_spinner)
        val modeList = listOf("Walking", "Driving", "Bicycling", "Transit")
        val modeAdapter = ArrayAdapter(this,
            R.layout.spinner_item, modeList)
        modeSpinner.adapter = modeAdapter
        modeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                mode = modeList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@AddEventActivity,
                    "Please select a mode.", Toast.LENGTH_SHORT).show()
            }
        }

        // VIEW MODEL AND CONNECTING TO EACH OF THE ADAPTERS
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        // observe our current list of events
        eventViewModel.readAllData.observe(this, Observer { events ->
            eventData = events.toMutableList()
        })
        // observe the address list we get from searching
        eventViewModel.placeData.observe(this, Observer {places ->
            placeData = places.toMutableList()
            var addressList = places.map{p -> p.name.replace(":","") +
                ": " + p.formatted_address.replace(":","")}
            addressAdapter.clear();
            addressAdapter.addAll(addressList)
            addressAdapter.notifyDataSetChanged();
        })

        // ADDING ON CLICKS FOR BUTTONS
        // Takes the search query and sends a search for places
        val addr_search_button = findViewById<Button>(R.id.addr_search)
        addr_search_button.setOnClickListener{
            var searchQuery = this.findViewById<TextView>(R.id.addr_input).text.toString()
            eventViewModel.getListOfPlaces(searchQuery, userLocation)
        }
        // On submit send out changes to destinations & add the event based on the correct origin
        val submit_button = findViewById<Button>(R.id.submit_event_button)
        submit_button.setOnClickListener {
            insertDataToDatabase()
        }
    }

    // HANDLES SHOWING THE TIME PICKER FOR OUR ARRIVAL TIME CHOOSER
    @RequiresApi(Build.VERSION_CODES.O)
    fun showTimePickerDialog(v: View) {
        // ONCE IT'S CLICKED WE SET THE VALUE FOR OUR ARRIVAL TIME
        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, i: Int, i1: Int ->
            hour = i; min = i1
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d",hour, min)
            // Calculates the EPOCH time stamp for the time
            val curDate = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = curDate.format(formatter) + " " + formattedTime
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val completeDate: Date = df.parse(formattedDate)
            // Sets the resulting time stamp here
            arrivalTime = completeDate.time/1000
            //Set new label
            val newBtnLabel = "Desired Arrival: $formattedTime"
            findViewById<Button>(R.id.event_arr_time_input).text = newBtnLabel
        }
        // DISPLAYS THE TIME PICKER
        val style = AlertDialog.THEME_HOLO_DARK;
        val timePickerDialog = TimePickerDialog(this, style, onTimeSetListener,
            hour, min, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    // HANDLES SUBMISSION INTO THE DATABASE BASED ON VALUES
    private fun insertDataToDatabase() {
        // Find the remaining values we need
        val title = findViewById<EditText>(R.id.event_title_input).text.toString()
        val desc = findViewById<EditText>(R.id.event_desc_input).text.toString()
        // Check values we need to verify
        if(inputCheck(title, address, arrivalTime, desc)){
            // Set origin as null or the given user's location, depending on permissions
            var origin: String? = userLocation

            // GO THROUGH EVENT DATA TO REORGANIZE THE LIST BASED ON ARRIVAL TIME
            eventData.sortBy{e -> e.arrivalTime}
            for (eIndex in 0 until eventData.size) {
                val curArrTime = eventData[eIndex].arrivalTime
                // We found an existing event that comes after us
                if (curArrTime > arrivalTime!!) {
                    // If there is something before us, that becomes our origin
                    if (eIndex-1 >= 0) {
                        origin = eventData[eIndex-1].address
                    }
                    // The existing event that is after us, takes our address as its origin
                    eventData[eIndex].origin = address
                    eventViewModel.getDirectionsAndUpdate(eventData[eIndex])
                    break
                }
                // if by the end of the loop we haven't found any existing event with a
                // later arrival time, that means that we are the last value
                else if (eIndex == eventData.size-1) {
                    origin = eventData[eIndex].address
                }
            }

            // Create Event Object
            val event = Event(
                0, title, type.lowercase(Locale.getDefault()),
                desc, origin, address!!.split(": ")[1],
                arrivalTime!!, mode.lowercase(Locale.getDefault()), "Undefined",
                -1
            )

            // Add Data to Database
            eventViewModel.getDirectionsAndAdd(event)

            // Create notification once Event is created
            val intent = Intent(this, ReminderBroadcast::class.java)
            intent.putExtra("title", event.title)
            val pendingIntent = PendingIntent.getBroadcast(this,
                event.arrivalTime.toInt(), intent,
                PendingIntent.FLAG_ONE_SHOT)
            // Set alarm for that notification
            val alarmManager = this.applicationContext?.getSystemService(ALARM_SERVICE) as AlarmManager
            val sixtyMinutesInSec = 60*60
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                (event.arrivalTime - sixtyMinutesInSec)*1000, pendingIntent)

            Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()

            // Navigate Back
            val goToListActivity = Intent(this, MainActivity::class.java)
            startActivity(goToListActivity)
        }else{
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_LONG).show()
        }
    }

    // Verify values that cannot be empty or null
    private fun inputCheck(title: String, addr: String?, arr_time: Long?, desc: String): Boolean{
        return !(TextUtils.isEmpty(title) || TextUtils.isEmpty(desc)
                || arr_time == null || addr == null)
    }

    // Standard menu inflater
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }
    // Standard options for menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Sends intent to the event list
            R.id.menu_item_event_list -> {
                val goToListActivity = Intent(this, MainActivity::class.java)
                startActivity(goToListActivity)
                true
            }
            R.id.menu_item_add_event -> {
                val goToListActivity = Intent(this, AddEventActivity::class.java)
                startActivity(goToListActivity)
                true
            }
            R.id.menu_item_weather -> {
                Toast.makeText(this, "Will send to add weather later.", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}