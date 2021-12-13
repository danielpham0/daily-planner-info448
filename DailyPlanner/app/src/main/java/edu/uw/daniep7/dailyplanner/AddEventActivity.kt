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
import android.text.Editable
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class AddEventActivity : AppCompatActivity() {
    private lateinit var eventViewModel: EventViewModel
    private lateinit var type: String
    private lateinit var mode: String
    private lateinit var address: String
    private var eventData = mutableListOf<Event>()
    private var placeData = mutableListOf<PlaceResult>()
    private var hour = 0
    private var min = 0
    private var dateTime: Long? = null
    private var userLocation: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        // ADDRESS SPINNER
        val addressSpinner = findViewById<Spinner>(R.id.event_addr_spinner)
        val addressList = mutableListOf<String>()
        val addressAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, addressList)
        addressSpinner.adapter = addressAdapter
        addressSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                address = addressList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@AddEventActivity,
                    "Please select an origin.", Toast.LENGTH_SHORT).show()
            }
        }

        // TYPE SPINNER
        val typeSpinner = findViewById<Spinner>(R.id.event_type_spinner)
        val typeList = listOf("Home", "Work", "School", "Food", "Meeting", "Shopping", "Other")
        val typeAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, typeList)
        typeSpinner.adapter = typeAdapter
        typeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
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
            android.R.layout.simple_spinner_item, modeList)
        modeSpinner.adapter = modeAdapter
        modeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                mode = modeList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@AddEventActivity,
                    "Please select a mode.", Toast.LENGTH_SHORT).show()
            }
        }

        // VIEW MODEL AND CONNECTING TO ADAPTERS
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        // observe our current list of events
        eventViewModel.readAllData.observe(this, Observer { events ->
            eventData = events.toMutableList()
        })
        // observe the address list we get from searching
        eventViewModel.placeData.observe(this, Observer {places ->
            placeData = places.toMutableList()
            var addressList = places.map{p -> p.name + "-" + p.formatted_address}
            addressAdapter.clear();
            addressAdapter.addAll(addressList)
            addressAdapter.notifyDataSetChanged();
        })

        // ADDING ON CLICKS
        val addr_search_button = findViewById<Button>(R.id.addr_search)
        addr_search_button.setOnClickListener{
            var searchQuery = this.findViewById<TextView>(R.id.addr_input).text.toString()
            eventViewModel.getListOfPlaces(searchQuery, userLocation)
        }
        // on submit send out changes to destinations + add the event based on the correct origin
        val submit_button = findViewById<Button>(R.id.submit_event_button)
        submit_button.setOnClickListener {
//            insertDataToDatabase()
            Toast.makeText(this@AddEventActivity,
                "Selected Values: type=$type", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showTimePickerDialog(v: View) {
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
            dateTime = completeDate.time

            //Set new label
            val newBtnLabel = "Desired Arrival: $formattedTime"
            findViewById<Button>(R.id.event_arr_time_input).text = newBtnLabel
        }

        val style = AlertDialog.THEME_HOLO_DARK;
        val timePickerDialog = TimePickerDialog(this, style, onTimeSetListener,
            hour, min, true)

        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun insertDataToDatabase() {
        val title = findViewById<EditText>(R.id.event_title_input).text.toString()
        val type = findViewById<EditText>(R.id.event_title_input).text.toString()
        val mode = findViewById<EditText>(R.id.event_title_input).text.toString()
        val addr = findViewById<EditText>(R.id.event_title_input).text.toString()
        val origin = findViewById<EditText>(R.id.event_title_input).text.toString()
        val arr_time = Integer.parseInt(findViewById<EditText>(R.id.event_title_input).text.toString())
        val desc = findViewById<EditText>(R.id.event_title_input).text.toString()

        if(true){
            // Create User Object
            val event = Event(
                0, title, type,
                desc, origin, addr,
                arr_time, mode, "Undefined",
                -1
            )
            // Add Data to Database
//            eventViewModel.getDirectionsAndAdd(event)
//
//            // Create notification once Event is created
//            val intent = Intent(this, ReminderBroadcast::class.java)
//            intent.putExtra("title", event.title)
//            val pendingIntent = PendingIntent.getBroadcast(this,
//                event.arrivalTime, intent,
//                PendingIntent.FLAG_ONE_SHOT)
//            // Set alarm for that notification
//            val alarmManager = this.applicationContext?.getSystemService(ALARM_SERVICE) as AlarmManager
//            val sixtyMinutesInMillis = 1000*60*60
//            alarmManager.set(
//                AlarmManager.RTC_WAKEUP,
//                (event.arrivalTime - sixtyMinutesInMillis).toLong(), pendingIntent)
//
//            Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()
//            // Navigate Back
            val goToListActivity = Intent(this, MainActivity::class.java)
            startActivity(goToListActivity)
        }else{
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_LONG).show()
        }
    }

    private fun inputCheck(firstName: String, lastName: String, age: Editable): Boolean{
        return !(TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && age.isEmpty())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

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