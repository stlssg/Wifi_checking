package it.polito.test.wifichecking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.DateTime

class CheckInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_check_in)

        val sharedPreferences = applicationContext.getSharedPreferences("AppSharedPreference", Context.MODE_PRIVATE)
        val savedNamePerson = sharedPreferences.getString("namePerson", "under-determined")
        val db = Firebase.firestore
        val now = DateTime.now()
        val input = hashMapOf(now.toString() to "IN")

        if (savedNamePerson != "under-determined") {
            db.collection("testShortcuts").document(savedNamePerson.toString()).set(input, SetOptions.merge())
        }

        Toast.makeText(applicationContext, "You check-in action has been recorded", Toast.LENGTH_LONG).show()

        finish()

    }
}