package it.polito.test.wifichecking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat.getAction
import androidx.core.view.accessibility.AccessibilityEventCompat.getAction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

//    lateinit var receiver: WifiConnectionChangedReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val start = findViewById<Button>(R.id.button)
        val textView1 = findViewById<TextView>(R.id.text1)
        val textView2 = findViewById<TextView>(R.id.text2)
        val editTextNamePerson = findViewById<TextView>(R.id.inputNamePerson)
        val editTextMaxPresence = findViewById<TextView>(R.id.inputMaxPresence)
        val editSSID = findViewById<TextView>(R.id.inputSSID)
        val sharedPreferences = applicationContext.getSharedPreferences("AppSharedPreference", Context.MODE_PRIVATE)

        val savedNamePerson = sharedPreferences.getString("namePerson", "under-determined")
        val savedMaxPresence = sharedPreferences.getString("maxPresence", "under-determined")
        val savedSSID = sharedPreferences.getString("SSID", "under-determined")
        val workState = sharedPreferences.getString("working_state", "STOPPED")

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssidString = wifiInfo.ssid
        val ssid = if (ssidString.startsWith("\"") && ssidString.endsWith("\"")) {
            ssidString.substring(1, ssidString.length -1)
        } else {
            ssidString
        }

        if (workState == "STOPPED") {
            start.text = "Start"
        } else {
            start.text = "Re-Start"
        }

        if (savedNamePerson != "under-determined") {
            editTextNamePerson.text = savedNamePerson?.replace("_", " ")
            editTextMaxPresence.text = savedMaxPresence
            editSSID.text = savedSSID
        } else {
            if (ssid == "<unknown ssid>") {
                editSSID.hint = "Input Wifi Name"
            } else {
                editSSID.text = ssid
            }
        }

//        receiver = WifiConnectionChangedReceiver()

        start.setOnClickListener{
            stopWork()
            start.text = "Re-Start"

            val namePerson = editTextNamePerson.text.toString().replace(" ", "_")
            val maxPresence = editTextMaxPresence.text.toString()
            val inputSSID = editSSID.text.toString()
            with(sharedPreferences.edit()) {
                putString("namePerson", namePerson)
                putString("maxPresence", maxPresence)
                putString("working_state", "RUNNING")
                putString("SSID", inputSSID)
                commit()
            }

            val db = Firebase.firestore
            val input = hashMapOf("SSID" to inputSSID, "Maximum_expected_number" to maxPresence)
            db.collection(inputSSID).document("Building_Information").set(input, SetOptions.merge())

            myWorkManager(namePerson, inputSSID)

//            simpleWork(bssid)

//            IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION).also {
//                registerReceiver(receiver, it)
//            }
        }

        val stop = findViewById<Button>(R.id.button2)
        stop.setOnClickListener{
            stopWork()
            start.text = "Start"
            with(sharedPreferences.edit()) {
                putString("working_state", "STOPPED")
                commit()
            }
//            unregisterReceiver(receiver)
        }

        val show = findViewById<Button>(R.id.button3)
        show.setOnClickListener{
            val newWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val newWifiInfo = newWifiManager.connectionInfo
            val newSSIDString = newWifiInfo.ssid
            val newNetId = newWifiInfo.networkId
            textView1.text = newWifiInfo.toString()
            if (newNetId != -1) {
                editSSID.text = if (newSSIDString.startsWith("\"") && newSSIDString.endsWith("\"")) {
                    newSSIDString.substring(1, newSSIDString.length-1)
                } else {
                    newSSIDString
                }
            }

            val workInfo = WorkManager.getInstance(this).getWorkInfosByTag("periodicWork")
            val listInfo = workInfo.get()
            if (listInfo == null || listInfo.size == 0) {
                Log.d("workInfo", "nothing is working")
                textView2.text = "no work is running currently"
            } else {
                for (info in listInfo) {
                    val workState = info.state.toString()
                    Log.d("workInfo", workState)
                    textView2.text = "the state of current work: $workState"
                }
            }

        }

        val grantLocationPermission = findViewById<Button>(R.id.button4)
        grantLocationPermission.setOnClickListener{
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        val stopBatteryOptimization = findViewById<Button>(R.id.button5)
        stopBatteryOptimization.setOnClickListener{
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT >= 25) {
            Shortcuts.setUp(applicationContext)
        }
    }

    private fun myWorkManager(namePerson: String, ssid: String) {
        val constraints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(false)
                .setRequiresDeviceIdle(false)
                .build()
        } else {
            Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(false)
                .build()
        }

        val myRequest = PeriodicWorkRequestBuilder<MyPeriodicWifiService>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("collection" to ssid, "Time_Start" to "07:00", "Time_End" to "23:00", "document" to "$namePerson+WIFI"))
            .setConstraints(constraints)
            .addTag("periodicWork")
//            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_checking_wifi",
            ExistingPeriodicWorkPolicy.KEEP,
//            ExistingPeriodicWorkPolicy.REPLACE,
            myRequest
        )
    }

    private fun stopWork() {
        WorkManager.getInstance().cancelAllWorkByTag("periodicWork")
    }


//    private fun simpleWork(bssid: String) {
//
//        val myRequest: WorkRequest = OneTimeWorkRequestBuilder<MyWifiService>()
//            .setInputData(workDataOf("Address" to bssid, "Time_Start" to "08:00", "Time_End" to "23:50"))
////            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//            .addTag("oneTimeWork")
//            .setInitialDelay(5, TimeUnit.SECONDS)
//            .build()
//        WorkManager.getInstance(this).enqueue(myRequest)
//    }


//            WorkManager.getInstance(this)
//                .getWorkInfosByTagLiveData("periodicWork")
//                .observe(this, Observer {
//                    it?.apply {
//                        when (this[0].state) {
//                            WorkInfo.State.BLOCKED -> println("BLOCKED")
//                            WorkInfo.State.CANCELLED -> println("CANCELLED")
//                            WorkInfo.State.RUNNING -> println("RUNNING")
//                            WorkInfo.State.ENQUEUED -> println("ENQUEUED")
//                            WorkInfo.State.FAILED -> println("FAILED")
//                            WorkInfo.State.SUCCEEDED -> println("SUCCEEDED")
//                            else -> println("else status ${this[0]}")
//                        }
//                    }
//                })

}