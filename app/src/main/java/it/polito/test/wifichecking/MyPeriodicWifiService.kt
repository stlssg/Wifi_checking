package it.polito.test.wifichecking

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.time.Instant.now
import java.time.LocalDateTime.now
import java.time.LocalTime.now
import java.util.concurrent.TimeUnit

class MyPeriodicWifiService(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    @SuppressLint("LongLogTag")
    override fun doWork(): Result {

        val now = DateTime.now()
        val start = inputData.getString("Time_Start").toString()
        val end = inputData.getString("Time_End").toString()

        if (isTimeInRange(now, start, end)) {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val db = Firebase.firestore
            val ssidString = wifiInfo.ssid
            val ssid = if (ssidString.startsWith("\"") && ssidString.endsWith("\"")) {
                ssidString.substring(1, ssidString.length - 1)
            } else {
                ssidString
            }
            val collectionPath = inputData.getString("collection").toString()
            val documentPath = inputData.getString("document").toString()
            val netID = wifiInfo.networkId
            if (netID != -1 && collectionPath == ssid) {
                val input = hashMapOf(now.toString() to "CONNECTED")
                db.collection(collectionPath).document(documentPath).set(input, SetOptions.merge())
            }

//            if (netID != -1) {
//                if (address == ssid) {
//                    val input = hashMapOf("timestamp" to now.toString(), "SSID" to ssid)
//                    db.collection(collectionPath).document(now.toString()).set(input, SetOptions.merge())
//                } else {
//                    val inputBssid1 = ssid?.toString() ?: "No_SSID"
//                    val input = hashMapOf("timestamp" to now.toString(), "SSID" to inputBssid1, "not_working" to "ssid_is_not_the_same")
//                    db.collection(collectionPath).document(now.toString()).set(input, SetOptions.merge())
//                }
//            } else {
//                val inputBssid2 = ssid?.toString() ?: "No_SSID"
//                val input = hashMapOf("timestamp" to now.toString(), "SSID" to inputBssid2, "not_working" to "no_connection")
//                db.collection(collectionPath).document(now.toString()).set(input, SetOptions.merge())
//            }

        }

        return Result.success()
    }

    private fun isTimeInRange(now: DateTime, start: String, end: String): Boolean {
        val format = DateTimeFormat.forPattern("HH:mm")
        val startTime: LocalTime = format.parseLocalTime(start)
        val endTime: LocalTime = format.parseLocalTime(end)
        val timeZone = DateTimeZone.getDefault()
        val today: LocalDate = LocalDate.now(timeZone)
        val startMoment: DateTime = today.toLocalDateTime(startTime).toDateTime(timeZone)
        val endMoment: DateTime = today.toLocalDateTime(endTime).toDateTime(timeZone)
        return now.isAfter(startMoment) && now.isBefore(endMoment)
    }

}