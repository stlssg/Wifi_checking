package it.polito.test.wifichecking

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.DateTime

class CheckOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_check_out)

        val sharedPreferences = applicationContext.getSharedPreferences("AppSharedPreference", Context.MODE_PRIVATE)
        val savedNamePerson = sharedPreferences.getString("namePerson", "under-determined")
        val db = Firebase.firestore
        val now = DateTime.now()
        val input = hashMapOf(now.toString() to "OUT")

        if (savedNamePerson != "under-determined") {
            db.collection("testShortcuts").document(savedNamePerson.toString()).set(input, SetOptions.merge())
        }

        Toast.makeText(applicationContext, "You check-out action has been recorded", Toast.LENGTH_LONG).show()

        finish()
    }
}