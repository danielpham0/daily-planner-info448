/** Daniel Pham: I wrote the methods in this reminder broadcast, which effectively displays
 * the notification. * */
package edu.uw.daniep7.dailyplanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Custom Broadcast Receiver based on our needs (DP)
class ReminderBroadcast: BroadcastReceiver() {
    // Once a notification intent is received, construct the notification and push it
    override fun onReceive(p0: Context?, p1: Intent?) {
        // Sets title based on what is passed in.
        val title: String = p1?.getStringExtra("title").toString()
        // Construct the notification
        val builder = p0?.let { NotificationCompat.Builder(it, "notifyEvent")
            .setSmallIcon(R.drawable.alarm_icon)
            .setContentTitle("Event Reminder")
            .setContentText("You have an event, '${title}', in 60 minutes.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)}
        val notificationManager = p0?.let { NotificationManagerCompat.from(it) }

        // Send the notification
        if (notificationManager != null) {
            if (builder != null) {
                notificationManager.notify(200,builder.build())
            }
        }

    }
}