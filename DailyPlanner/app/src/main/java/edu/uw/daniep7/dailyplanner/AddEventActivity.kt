package edu.uw.daniep7.dailyplanner

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
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
    private lateinit var origin: String
    private var eventData = mutableListOf<Event>()
    private var placeData = mutableListOf<PlaceResult>()
    private var hour = 0
    private var min = 0
    private var dateTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // ORIGIN SPINNER
        val originSpinner = findViewById<Spinner>(R.id.event_origin_spinner)
        val originList = mutableListOf<String>()
        val originAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, originList)
        originSpinner.adapter = originAdapter
        originSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                origin = originList[position]
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

        // Instantiate View Model and connect it to the adapter
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        eventViewModel.readAllData.observe(this, Observer { events ->
            eventData = events.toMutableList()
            var addressList = events.map{e -> e.address}
            addressList = listOf("Current Location") + addressList
            originAdapter.clear();
            originAdapter.addAll(addressList)
            originAdapter.notifyDataSetChanged();
        })
//        eventViewModel.placeData.observe(this, Observer {places ->
//            placeData = places.toMutableList()
//            originAdapter.clear();
//            originAdapter.addAll(addressList)
//            originAdapter.notifyDataSetChanged();
//        })

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
            eventViewModel.getDirectionsAndAdd(event)

            // Create notification once Event is created
            val intent = Intent(this, ReminderBroadcast::class.java)
            intent.putExtra("title", event.title)
            val pendingIntent = PendingIntent.getBroadcast(this,
                event.arrivalTime, intent,
                PendingIntent.FLAG_ONE_SHOT)
            // Set alarm for that notification
            val alarmManager = this.applicationContext?.getSystemService(ALARM_SERVICE) as AlarmManager
            val sixtyMinutesInMillis = 1000*60*60
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                (event.arrivalTime - sixtyMinutesInMillis).toLong(), pendingIntent)

            Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()
            // Navigate Back
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