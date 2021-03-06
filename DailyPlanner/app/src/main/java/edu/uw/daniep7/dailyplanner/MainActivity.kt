/** Daniel Pham: I wrote the code for this Main Activity, including the menus listed. * */
package edu.uw.daniep7.dailyplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
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