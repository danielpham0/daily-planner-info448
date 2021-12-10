/** Daniel Pham: I wrote all of the code for this Main Activity, including the Event
 *  data class and the menus listed. * */
package edu.uw.daniep7.dailyplanner

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.uw.daniep7.dailyplanner.network.EventListViewModel
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.io.FileOutputStream

@Parcelize
data class Event (
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

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_event_list -> {
                val goToListActivity = Intent(this, MainActivity::class.java)
                startActivity(goToListActivity)
                true
            }
            R.id.menu_item_add_event -> {
                Toast.makeText(this, "Will send to add event later.", Toast.LENGTH_SHORT).show()
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